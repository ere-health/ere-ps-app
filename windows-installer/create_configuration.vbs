Set objFSO = CreateObject("Scripting.FileSystemObject")
Set objFile = objFSO.OpenTextFile("service_installer_conf_template.xml", 1)
currentdir = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)

strText = objFile.ReadAll
objFile.Close
strNewText = Replace(strText, "$PATH", currentdir)

Set objFile = objFSO.CreateTextFile("service_installer.xml", 2)
objFile.Write strNewText
objFile.Close
