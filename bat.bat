@echo off
REM path of the json
set file=C:\JsonExtrator\json.json 
setlocal enableDelayedExpansion

set numT=0 
set numX=0
set title=title
set Suite=Suite
for /f "tokens=1,2 delims=:{}[], " %%a in (%file%) do (
set "VAR=%%~a"
set VAR=!VAR:	=!
set VAR=!VAR:"=!
if "x!VAR:%title%=!"=="x%VAR%" (
		set /A numX+=1
		set "elem[!numX!]=%%~b"
		
	)
if "x!VAR:%Suite%=!"=="x%VAR%" (
		set /A numT+=1
		set "elx[!numT!]=%%~b"
	)
)

REM below is the query that will execute
cd /d SolutionProject\SolutionProject\bin\Debug 
FOR /L %%i IN (1,1,%numT%) DO (
Proj_TeamCenter.exe /ts:!elx[%%i]!.rxtst /tc:!elem[%%i]!
)

endlocal
