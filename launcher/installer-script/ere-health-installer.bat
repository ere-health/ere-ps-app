@ECHO OFF
echo[
echo Welcome to the installation process of the application ere.health!
echo[
echo[
echo[
echo[
echo[

if exist jre\ (
    echo The Java folder is already present, skipping the download
) else (
    echo The Java folder is missing, downloading it
  powershell -Command "Invoke-WebRequest -Uri https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11+9/OpenJDK11U-jre_x86-32_windows_hotspot_11.0.11_9.zip -OutFile jre.zip"
  powershell -Command "Expand-Archive jre.zip

  del jre.zip
  xcopy jre\jdk-11.0.11+9-jre\* jre /E
  rmdir jre\jdk-11.0.11+9-jre /s/q
)

echo[
echo[
echo The launcher is being downloaded
echo[
powershell -Command "Invoke-WebRequest -Uri https://ere.health/ere-health-launcher.exe -OutFile ere-health-launcher.exe"

echo[
echo Now starting the launcher
echo[

start ere-health-launcher.exe
