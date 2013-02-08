@echo off
echo.
echo example of usage
echo hostActivities.cmd com.github.vmorev.crawler.awsflow.activity.ArticleCrawlerActivitiesImpl 1
echo hostActivities.cmd com.github.vmorev.crawler.awsflow.activity.SiteCrawlerActivitiesImpl 1
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
java -cp %classpath% com.github.vmorev.crawler.tools.ActivityHoster %*
popd

pause
