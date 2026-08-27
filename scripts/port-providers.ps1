$sourceProviders = "c:\MacBookLinux\streamflix\app\src\main\java\com\streamflixreborn\streamflix\providers"
$sourceExtractors = "c:\MacBookLinux\streamflix\app\src\main\java\com\streamflixreborn\streamflix\extractors"
$destProviders = "c:\MacBookLinux\streamflix-linux\providers\src\main\kotlin\com\streamflixreborn\streamflix\providers"
$destExtractors = "c:\MacBookLinux\streamflix-linux\providers\src\main\kotlin\com\streamflixreborn\streamflix\extractors"

# Create destination directories if they don't exist
New-Item -ItemType Directory -Force -Path $destProviders | Out-Null
New-Item -ItemType Directory -Force -Path $destExtractors | Out-Null

$providersCount = 0
$extractorsCount = 0

function Process-File {
    param($src, $dst)
    $content = Get-Content -Path $src -Raw
    
    # Imports to replace
    $content = $content -replace 'import android\.util\.Log', 'import com.streamflixreborn.streamflix.compat.Log'
    $content = $content -replace 'import android\.util\.Base64', 'import java.util.Base64'
    $content = $content -replace 'import android\.net\.Uri', 'import java.net.URI'
    $content = $content -replace '(?m)^.*import android\.os\.Parcelable.*$\r?\n?', ''
    $content = $content -replace '(?m)^.*import kotlinx\.parcelize\.Parcelize.*$\r?\n?', ''
    $content = $content -replace '(?m)^.*import androidx\.room\..*$\r?\n?', ''
    $content = $content -replace 'import com\.streamflixreborn\.streamflix\.adapters\.AppAdapter', 'import com.streamflixreborn.streamflix.compat.Item'
    
    # Base64 replacements
    $content = $content -replace 'Base64\.decode\(([^,]+),\s*Base64\.[A-Z_]+\)', 'Base64.getDecoder().decode($1)'
    $content = $content -replace 'Base64\.encodeToString\(([^,]+),\s*Base64\.[A-Z_]+\)', 'Base64.getEncoder().encodeToString($1)'
    $content = $content -replace 'Base64\.encode\(([^,]+),\s*Base64\.[A-Z_]+\)', 'Base64.getEncoder().encode($1)'
    
    # Annotations
    $content = $content -replace '(?m)^.*@Parcelize.*$\r?\n?', ''
    $content = $content -replace '(?m)^.*@Entity\(.*$\r?\n?', ''
    $content = $content -replace '(?m)^.*@PrimaryKey.*$\r?\n?', ''
    $content = $content -replace '(?m)^.*@Embedded.*$\r?\n?', ''
    $content = $content -replace '(?m)^.*@Ignore.*$\r?\n?', ''
    
    # Interfaces and Types
    $content = $content -replace ':\s*Parcelable', ''
    $content = $content -replace ',\s*Parcelable', ''
    $content = $content -replace 'AppAdapter\.Item', 'Item'
    $content = $content -replace 'AppAdapter\.Type', 'String'
    $content = $content -replace '(?m)^.*override lateinit var itemType.*$\r?\n?', ''
    
    # Write to destination
    Set-Content -Path $dst -Value $content -NoNewline
}

Get-ChildItem -Path $sourceProviders -Filter *.kt -File | ForEach-Object {
    $dst = Join-Path -Path $destProviders -ChildPath $_.Name
    Process-File -src $_.FullName -dst $dst
    $script:providersCount++
}

Get-ChildItem -Path $sourceExtractors -Filter *.kt -File | ForEach-Object {
    $dst = Join-Path -Path $destExtractors -ChildPath $_.Name
    Process-File -src $_.FullName -dst $dst
    $script:extractorsCount++
}

# Special case for Provider.kt
$providerInterfacePath = Join-Path -Path $destProviders -ChildPath "Provider.kt"
if (Test-Path $providerInterfacePath) {
    $content = Get-Content -Path $providerInterfacePath -Raw
    $content = $content -replace 'List<AppAdapter\.Item>', 'List<Item>'
    Set-Content -Path $providerInterfacePath -Value $content -NoNewline
}

Write-Host "Processed $providersCount provider files and $extractorsCount extractor files."
