package org.micromanager.utils;


import bsh.Interpreter;

public interface ScriptingEngine {
   public void evaluate(String script) throws MMScriptException;
   public void evaluateAsync(String script)throws MMScriptException;
   public void insertGlobalObject(String name, Object obj) throws MMScriptException;
   public void stopRequest();
   public boolean stopRequestPending();
   public void sleep(long ms) throws MMScriptException;
   public void setInterpreter(Interpreter interp);
   public void resetInterpreter();
}
