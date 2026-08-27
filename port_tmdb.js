const fs = require('fs');
const path = require('path');

function processFile(src, dest) {
    let content = fs.readFileSync(src, 'utf8');
    
    // 1. Replace import android.util.Log -> import com.streamflixreborn.streamflix.compat.Log
    content = content.replace(/import android\.util\.Log/g, "import com.streamflixreborn.streamflix.compat.Log");
    
    // 2. Replace import android.util.Base64 -> import java.util.Base64
    content = content.replace(/import android\.util\.Base64/g, "import java.util.Base64");
    
    // 3. Base64.decode
    content = content.replace(/Base64\.decode\(([^,]+),\s*Base64\.DEFAULT\)/g, "Base64.getDecoder().decode($1)");
    
    // 4. Base64.encodeToString
    content = content.replace(/Base64\.encodeToString\(([^,]+),\s*Base64\.NO_WRAP\)/g, "Base64.getEncoder().encodeToString($1)");
    
    // 5. Remove any imports of android.* classes
    content = content.replace(/^import android\..*$/gm, "");
    
    // 6. Replace AppAdapter import
    content = content.replace(/import com\.streamflixreborn\.streamflix\.adapters\.AppAdapter/g, "import com.streamflixreborn.streamflix.compat.Item");
    
    // 7. Replace AppAdapter.Item -> Item
    content = content.replace(/AppAdapter\.Item/g, "Item");
    
    // 8. Replace AppAdapter.Type -> String
    content = content.replace(/AppAdapter\.Type/g, "String");
    
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
