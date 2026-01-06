$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
$zipPath = "apache-maven.zip"
$extractPath = "."

Write-Host "Downloading Maven from $mavenUrl..."
Invoke-WebRequest -Uri $mavenUrl -OutFile $zipPath

Write-Host "Extracting Maven..."
Expand-Archive -Path $zipPath -DestinationPath $extractPath -Force

$mavenFolder = Get-ChildItem -Path $extractPath -Filter "apache-maven*" -Directory | Select-Object -First 1
$mavenBinPath = $mavenFolder.FullName + "\bin"


Write-Host "Setting environment variables..."
$javaExe = (Get-Command java).Source
$javaHome = Split-Path (Split-Path $javaExe -Parent) -Parent
$env:JAVA_HOME = $javaHome
$env:Path += ";$mavenBinPath"


Write-Host "Verifying Maven installation..."
mvn -version

Write-Host "Maven setup complete. You can now run the app."
