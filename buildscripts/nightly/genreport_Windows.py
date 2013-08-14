# Python >= 3.3

# Generate a summary report from a build log file.

# This program is necessarily written in a rather ad-hoc style, since the XML
# log written by Ant is not very structured.


import os.path
import re
import sys
import traceback
from xml.etree import ElementTree


def build_status_report(section_sink, log_filename, build):
    title = "Build Status"
    text = "Log file: {}\n".format(os.path.abspath(log_filename))
    error = build.findall(".")[0].attrib.get("error")
    if error:
        is_error = True
        text += "Build terminated with error:\n"
        text += error
    else:
        is_error = False
        text += "Build completed without fatal error"
    section_sink.send((is_error, title, text))


def cpp_build_report(section_sink, build_cpp_target, architecture):
    build_cpp_task = build_cpp_target.findall("./task[@name='buildcpp']")[0]
    cpp_info_messages = build_cpp_task.findall("./message[@priority='info']")
    cpp_info_lines = (e.findtext(".") for e in cpp_info_messages)

    # TODO With VS2010 and MSBuild, all lines will have the build order prefix
    # (\d+>), and all will be from a single call to MSBuild. With that
    # arrangement, it will be easy to extract the error messages.
    errors, warnings = list(), list()
    for line in cpp_info_lines:
        m = re.match(r"(\d+>)?([^ ]+) - (\d+) error\(s\), (\d+) warning\(s\)$", line)
        if m:
            order, project, n_errors, n_warnings = m.groups()
            n_errors, n_warnings = int(n_errors), int(n_warnings)
            if n_errors:
                errors.append((project, n_errors, n_warnings))
            elif n_warnings:
                warnings.append((project, n_warnings))
    if errors:
        title = "C++ Errors ({})".format(architecture)
        text = "\n".join("{}: {} error(s)".format(project, n_errors)
                         for project, n_errors, n_warnings
                         in sorted(errors, key=lambda x: x[0]))
        section_sink.send((True, title, text))
    if warnings:
        title = "C++ Warnings ({})".format(architecture)
        text = "\n".join("{}: {} warning(s)".format(project, n_warnings)
                         for project, n_warnings
                         in sorted(warnings, key=lambda x: x[0]))
        section_sink.send((False, title, text))


def java_build_report(section_sink, stage_target, architecture):
    mm_javac_tasks = stage_target.findall(".//task[@name='mm-javac']")
    for task in mm_javac_tasks:
        java_warn_messages = task.findall("./message[@priority='warn']")
        java_warn_lines = (e.findtext(".") for e in java_warn_messages)
        n_errors, n_warnings = 0, 0
        filtered_lines = list()
        for line in java_warn_lines:
            m = re.match(r"(\d+) errors", line)
            if m:
                n_errors = int(m.group(1))
                filtered_lines.append(line)
                continue
            m = re.match(r"(\d+) warnings$", line)
            if m:
                n_warnings = int(m.group(1))
                filtered_lines.append(line)
                continue
            m = re.match(r"Note: ", line)
            if m:
                # For now, ignore "Note" lines as there are many.
                continue
            filtered_lines.append(line)

        text = "\n".join(filtered_lines).strip()
        if text:
            if n_errors:
                is_error = True
                title = "Java errors (during {} build)".format(architecture)
            elif n_warnings:
                is_error = False
                title = "Java warnings (during {} build)".format(architecture)
            else:
                assert False, "Unexpected javac message format"
            section_sink.send((is_error, title, text))


def clojure_build_report(section_sink, stage_target, architecture):
    # For now, cheat under the assumption that all 'java' tasks are the Clojure
    # compiler.
    mm_cljc_tasks = stage_target.findall(".//target[@name='compile']/task[@name='java']")
    for task in mm_cljc_tasks:
        clj_info_messages = task.findall("./message[@priority='info']")
        clj_info_lines = (e.findtext(".") for e in clj_info_messages)
        lines_before_stacktrace = list()
        is_error = False
        for line in clj_info_lines:
            m = re.match(r"Exception in thread ", line)
            if m:
                is_error = True
                lines_before_stacktrace.append(line)
                break
            lines_before_stacktrace.append(line)

        text = "\n".join(lines_before_stacktrace).strip()
        if is_error and text:
            title = "Clojure errors (during {} build)".format(architecture)
            section_sink.send((True, title, text))


def generate_report(section_sink, log_filename, build):
    build_status_report(section_sink, log_filename, build)

    # At the top level, there are task[@name='ant'] elements for unstage-all,
    # clean-all, package/upload (x64), and package/upload(Win32). Each of these
    # tasks contain an e.g. target[@name='unstage-all'] elements.

    sequential_tasks = build.findall("./task[@name='ant']")
    # Sorry, this is rather fragile...
    seen_cpp_x64 = False
    seen_cpp_Win32 = False

    for task in sequential_tasks:
        targets = task.findall("./target")
        target_names = list(target.attrib["name"] for target in targets)
        if "unstage-all" in target_names or "clean-all" in target_names:
            continue
        if "build-cpp" in target_names:
            assert not seen_cpp_Win32
            if seen_cpp_x64:
                arch = "Win32"
                seen_cpp_Win32 = True
            else:
                arch = "x64"
                seen_cpp_x64 = True

            cpp_build_report(section_sink,
                             task.findall("./target[@name='build-cpp']")[0],
                             arch)
        if "stage" in target_names:
            if seen_cpp_Win32:
                arch = "Win32"
            else:
                assert seen_cpp_x64
                arch = "x64"

            java_build_report(section_sink,
                              task.findall("./target[@name='stage']")[0],
                              arch)
            clojure_build_report(section_sink,
                                 task.findall("./target[@name='stage']")[0],
                                 arch)


def section_writer(file):
    any_critical = False
    while True:
        is_critical, title, text = yield
        any_critical = any_critical or is_critical
        if is_critical:
            print("<p><i><b>{}</b></i></p>".format(title), file=file)
        else:
            print("<p><i>{}</i></p>".format(title), file=file)
        print("<pre>{}</pre>".format(text), file=file)


def main(src_root, log_filename, output_filename):
    with open(output_filename, "w") as f:
        section_sink = section_writer(f)
        next(section_sink)  # Prime the coroutine

        generate_report(section_sink,
                        log_filename,
                        ElementTree.parse(log_filename))


if __name__ == "__main__":
    try:
        src_root, log_filename, output_filename = sys.argv[1:]
    except:
        print("Usage: python {} src_root log_filename output_filename".
              format(sys.argv[0]))
        sys.exit(2)

    main(src_root, log_filename, output_filename)
