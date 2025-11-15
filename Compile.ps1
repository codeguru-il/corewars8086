# compile.ps1

# --- Compile Survivors ---
Write-Host "Compiling survivors..."
$survivorFiles = Get-ChildItem -Path ".\survivors\" -Filter "*.asm"
foreach ($file in $survivorFiles) {
    $outputName = $file.BaseName # Gets the filename without the extension
    Write-Host "  Assembling $($file.Name) -> $outputName"
    nasm -f bin -o "survivors\$outputName" $file.FullName
}

# --- Compile Zombies ---
Write-Host "Compiling zombies..."
$zombieFiles = Get-ChildItem -Path ".\zombies\" -Filter "*.asm"
foreach ($file in $zombieFiles) {
    $outputName = $file.BaseName # Gets the filename without the extension
    Write-Host "  Assembling $($file.Name) -> $outputName"
    nasm -f bin -o "zombies\$outputName" $file.FullName
}

Write-Host "Compilation complete."