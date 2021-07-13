@ECHO OFF

powershell -Command "Invoke-WebRequest -Uri https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11+9/OpenJDK11U-jre_x86-32_windows_hotspot_11.0.11_9.zip -OutFile jre.zip"

powershell -Command "Expand-Archive jre.zip

del jre.zip

xcopy jre\jdk-11.0.11+9-jre\* jre /E

rmdir jre\jdk-11.0.11+9-jre /s/q

powershell -Command "Invoke-WebRequest -Uri http://ec2-18-219-70-177.us-east-2.compute.amazonaws.com/ere-health-launcher.exe -OutFile ere-health-launcher.exe"

start ere-health-launcher.exe
