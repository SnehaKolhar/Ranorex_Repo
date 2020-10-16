@echo on
set project_path=%CD%
echo %project_path%
%project_path%\zulu-8-jre\bin\java.exe -jar %project_path%\polarion-test-runs-extractor.jar drivepilot_std "waiting" %project_path%\settings.properties