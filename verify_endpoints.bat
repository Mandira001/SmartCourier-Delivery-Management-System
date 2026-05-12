@echo off
setlocal EnableDelayedExpansion
rem ---- Login and obtain JWT token ----
for /f "delims=" %%A in ('curl -s -X POST http://localhost:8080/auth/auth/login -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\",\"password\":\"UserPass123\"}"') do set TOKEN=%%A
echo JWT Token: !TOKEN!

rem ---- Helper function (using curl) ----
:callApi
set METHOD=%1
set URL=%2
set BODY=%3
if "!BODY!"=="" (
  curl -s -X !METHOD! -H "Authorization: Bearer !TOKEN!" http://localhost:8080!URL!
) else (
  curl -s -X !METHOD! -H "Authorization: Bearer !TOKEN!" -H "Content-Type: application/json" -d "!BODY!" http://localhost:8080!URL!
)
exit /b

rem ---- Admin endpoints ----
call :callApi GET /admin/admin/dashboard
call :callApi GET /admin/admin/deliveries
call :callApi GET /admin/admin/revenue-trend

rem ---- Delivery endpoints (user) ----
call :callApi GET /deliveries/deliveries/my
rem sample creation payload (adjust fields as per DTO)
set CREATE_PAYLOAD={"senderAddress":{"street":"A St","city":"City","state":"ST","zip":"12345"},"receiverAddress":{"street":"B St","city":"Town","state":"TS","zip":"54321"},"packageWeight":2.5}
call :callApi POST /deliveries/deliveries !CREATE_PAYLOAD!

rem ---- Tracking endpoint ----
call :callApi GET /tracking/tracking/ABC123

rem ---- Admin delivery status update (example id=1) ----
call :callApi PUT /admin/admin/deliveries/1/resolve?status=DELIVERED

endlocal
