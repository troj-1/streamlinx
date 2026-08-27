const fs = require('fs');
const path = require('path');

function processFile(src, dest) {
    let content = fs.readFileSync(src, 'utf8');
    
    // System Prompt Imports
    content = content.replace(/import android\.os\.Parcelable\n/g, "");
    content = content.replace(/import kotlinx\.parcelize\.Parcelize\n/g, "");
    content = content.replace(/import androidx\.room\..*\n/g, "");
    content = content.replace(/import com\.streamflixreborn\.streamflix\.adapters\.AppAdapter\n/g, "import com.streamflixreborn.streamflix.compat.Item\n");
    content = content.replace(/import android\.content\.Context\n/g, "");
    content = content.replace(/import android\.webkit\..*\n/g, "");
    content = content.replace(/import androidx\.leanback\..*\n/g, "");
    content = content.replace(/import com\.bumptech\.glide\..*\n/g, "");
    
    // Other imports replacements
    content = content.replace(/import android\.util\.Base64/g, "import java.util.Base64");
    content = content.replace(/import android\.util\.Log/g, "import com.streamflixreborn.streamflix.compat.Log");
    content = content.replace(/import android\.net\.Uri/g, "import java.net.URI");
    
    // 5. Remove any other imports of android.* classes
    content = content.replace(/^import android\..*$/gm, "");
    
    // Annotations to REMOVE
    content = content.replace(/@Parcelize\s*\n?/g, "");
    content = content.replace(/@Entity\([^)]*\)\s*\n?/g, "");
    content = content.replace(/@Entity\s*\n?/g, "");
    content = content.replace(/@PrimaryKey\([^)]*\)\s*\n?/g, "");
    content = content.replace(/@PrimaryKey\s*\n?/g, "");
    content = content.replace(/@Embedded\s*\n?/g, "");
    content = content.replace(/@Ignore\s*\n?/g, "");
    content = content.replace(/@Index\([^)]*\)\s*\n?/g, "");
    
    // Class declaration changes
    content = content.replace(/:\s*Parcelable\s*,/g, ":");
    content = content.replace(/,\s*Parcelable/g, "");
    content = content.replace(/:\s*Parcelable/g, "");
    
    // Base64 replacements
    content = content.replace(/Base64\.decode\(([^,]+),\s*Base64\.DEFAULT\)/g, "Base64.getDecoder().decode($1)");
    content = content.replace(/Base64\.encodeToString\(([^,]+),\s*Base64\.NO_WRAP\)/g, "Base64.getEncoder().encodeToString($1)");
    content = content.replace(/Base64\.encode\(([^,]+),\s*Base64\.NO_WRAP\)/g, "Base64.getEncoder().encode($1)");
    
    // Replace AppAdapter.Item -> Item
    content = content.replace(/AppAdapter\.Item/g, "Item");
    
    // Replace AppAdapter.Type -> String
    content = content.replace(/AppAdapter\.Type/g, "String");
    content = content.replace(/ItemType/g, "String"); // From system instructions, AppAdapter.Type is ItemType usually? Wait, the user prompt said "Replace AppAdapter.Type with String everywhere".
    
    // 9. Remove override lateinit var itemType lines
    content = content.replace(/^\s*override\s+lateinit\s+var\s+itemType.*$/gm, "");
    content = content.replace(/^\s*override\s+var\s+itemType.*$/gm, "");
    
    // 10. BuildConfig -> "mock_build_config" or similar?
    content = content.replace(/BuildConfig\.VERSION_NAME/g, '"1.0.0"');
    content = content.replace(/BuildConfig\.VERSION_CODE/g, '1');
    content = content.replace(/BuildConfig\.APPLICATION_ID/g, '"com.streamflixreborn.streamflix"');
    content = content.replace(/BuildConfig\.DEBUG/g, 'false');
    
    // 11. StreamFlixApp or Context
    content = content.replace(/StreamFlixApp\.context/g, "null");
    content = content.replace(/StreamFlixApp\.getInstance\(\)/g, "null");
    
    // ensure dest dir exists
    const destDir = path.dirname(dest);
    if (!fs.existsSync(destDir)) {
        fs.mkdirSync(destDir, { recursive: true });
    }
    fs.writeFileSync(dest, content, 'utf8');
    
    console.log(`Processed ${src} to ${dest}`);
}

processFile("c:\\MacBookLinux\\streamflix\\app\\src\\main\\java\\com\\streamflixreborn\\streamflix\\utils\\TMDb3.kt",
            "c:\\MacBookLinux\\streamflix-linux\\core\\src\\main\\kotlin\\com\\streamflixreborn\\streamflix\\utils\\TMDb3.kt");
            
processFile("c:\\MacBookLinux\\streamflix\\app\\src\\main\\java\\com\\streamflixreborn\\streamflix\\utils\\TmdbUtils.kt",
            "c:\\MacBookLinux\\streamflix-linux\\core\\src\\main\\kotlin\\com\\streamflixreborn\\streamflix\\utils\\TmdbUtils.kt");
