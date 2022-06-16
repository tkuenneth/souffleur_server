$java_home = "C:\Program Files\Amazon Corretto\jdk17.0.1_12"
$base_dir = "C:\Users\tkuen\Entwicklung\GitHub\souffleur\server"

$version = "???"
$source = Get-Content -Path $base_dir\src\eu\thomaskuenneth\souffleur\Main.java
foreach($line in $source) {
    if ($line -match "VERSION = `"(.+)`"") {
        $version = $matches[1]
        break
    }
}

Set-Location $base_dir

Write-Output "java_home: $java_home"
Write-Output "base_dir: $base_dir"
Write-Output "version: $version"

$command = "$java_home\bin\jpackage.exe"
$arguments = "--win-menu --win-menu-group `"Thomas Kuenneth`" --vendor `"Thomas Kuenneth`" --name Souffleur --icon $base_dir\artwork\Souffleur.ico --type msi --app-version $version --input $base_dir\out\artifacts\server_jar --main-jar server.jar"

Write-Output $arguments

Start-Process -RedirectStandardOutput stdout.txt -RedirectStandardError stderr.txt -FilePath $command -ArgumentList $arguments -Wait