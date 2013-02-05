@echo off
echo.
echo example of usage
echo startWorkflow.cmd site all
echo startWorkflow.cmd site site.json
echo startWorkflow.cmd article article.json
echo.

set java_home=c:\bin\jdk7
set path=%java_home%\bin;%path%

setlocal enableextensions
setlocal enabledelayedexpansion

pushd .
cd /d %~dp0%\lib
set classpath=lib\
for %%i in (*.jar) do (
    set classpath=!classpath:#= !;lib\%%i
)
popd

pushd .
cd /d %~dp0%
java -cp %classpath% com.github.vmorev.crawler.tools.WorkflowExecutionStarter %*
popd

pause
