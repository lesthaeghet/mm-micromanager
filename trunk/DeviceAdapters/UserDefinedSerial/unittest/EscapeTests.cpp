// DESCRIPTION:   Unit tests for UserDefinedSerial
//
// COPYRIGHT:     University of California San Francisco, 2014
//
// LICENSE:       This file is distributed under the BSD license.
//                License text is included with the source distribution.
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// AUTHOR:        Mark Tsuchida

#include <gtest/gtest.h>

#include "UserDefinedSerial.h"


class ParameterizedEscapeTest : public ::testing::Test,
   public ::testing::WithParamInterface< std::pair<std::vector<char>,
   const char*> >
{
protected:
   std::vector<char> input_;
   std::string expectedOutput_;
   virtual void SetUp()
   {
      input_ = GetParam().first;
      expectedOutput_ = GetParam().second;
   }
};

TEST_P(ParameterizedEscapeTest, Basic)
{
   ASSERT_EQ(expectedOutput_, EscapedStringFromByteString(input_));
}

inline std::vector<char> NullFollowedByX()
{
   std::vector<char> ret(2, '\0');
   ret[1] = 'X';
   return ret;
}

INSTANTIATE_TEST_CASE_P(BasicTestCase, ParameterizedEscapeTest,
   ::testing::Values(
      // Trivial
      std::make_pair(std::vector<char>(), ""),
      std::make_pair(std::vector<char>(1, 'x'), "x"),
      std::make_pair(std::vector<char>(2, 'x'), "xx"),
      std::make_pair(std::vector<char>(3, 'x'), "xxx"),
      // Backslash
      std::make_pair(std::vector<char>(1, '\\'), "\\\\"),
      std::make_pair(std::vector<char>(2, '\\'), "\\\\\\\\"),
      // Single-character escapes
      std::make_pair(std::vector<char>(1, '\n'), "\\n"),
      std::make_pair(std::vector<char>(2, '\n'), "\\n\\n"),
      // Null char
      std::make_pair(std::vector<char>(1, '\0'), "\\0"),
      std::make_pair(std::vector<char>(2, '\0'), "\\0\\0"),
      std::make_pair(NullFollowedByX(), "\\0X"),
      // Non-null control char
      std::make_pair(std::vector<char>(1, '\1'), "\\x01"),
      std::make_pair(std::vector<char>(2, '\1'), "\\x01\\x01"),
      // Edge cases
      std::make_pair(std::vector<char>(1, '~'), "~"),
      std::make_pair(std::vector<char>(1, 127), "\\x7f"),
      std::make_pair(std::vector<char>(1, ' '), " "),
      std::make_pair(std::vector<char>(1, '\x1f'), "\x1f")
   ));


int main(int argc, char **argv)
{
   ::testing::InitGoogleTest(&argc, argv);
   return RUN_ALL_TESTS();
}
