import os
import re

def process_file(src, dest):
    with open(src, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 1. Replace import android.util.Log -> import com.streamflixreborn.streamflix.compat.Log
    content = content.replace("import android.util.Log", "import com.streamflixreborn.streamflix.compat.Log")
    
    # 2. Replace import android.util.Base64 -> import java.util.Base64
    content = content.replace("import android.util.Base64", "import java.util.Base64")
    
    # 3. Base64.decode
    content = re.sub(r'Base64\.decode\(([^,]+),\s*Base64\.DEFAULT\)', r'Base64.getDecoder().decode(\1)', content)
    
    # 4. Base64.encodeToString
    content = re.sub(r'Base64\.encodeToString\(([^,]+),\s*Base64\.NO_WRAP\)', r'Base64.getEncoder().encodeToString(\1)', content)
    
    # 5. Remove any imports of android.* classes (except ones already replaced if any remain)
    # wait, Log and Base64 were android.util, which we already replaced. Let's just remove any starting with import android.
    content = re.sub(r'^import android\..*$\n?', '', content, flags=re.MULTILINE)
    
    # 6. Replace AppAdapter import
    content = content.replace("import com.streamflixreborn.streamflix.adapters.AppAdapter", "import com.streamflixreborn.streamflix.compat.Item")
    
    # 7. Replace AppAdapter.Item -> Item
    content = content.replace("AppAdapter.Item", "Item")
    
    # 8. Replace AppAdapter.Type -> String
    content = content.replace("AppAdapter.Type", "String")
    
    # 9. Remove override lateinit var itemType lines
    content = re.sub(r'\s*override\s+lateinit\s+var\s+itemType.*$\n?', '\n', content, flags=re.MULTILINE)
    content = re.sub(r'\s*override\s+var\s+itemType.*$\n?', '\n', content, flags=re.MULTILINE)
    
    # 10. BuildConfig -> "mock_build_config" or similar?
    content = content.replace("BuildConfig.VERSION_NAME", '"1.0.0"')
    content = content.replace("BuildConfig.VERSION_CODE", '1')
    content = content.replace("BuildConfig.APPLICATION_ID", '"com.streamflixreborn.streamflix"')
    content = content.replace("BuildConfig.DEBUG", 'false')
    
    # 11. StreamFlixApp or Context
    content = content.replace("StreamFlixApp.context", "null")
    content = content.replace("StreamFlixApp.getInstance()", "null")
    
    # ensure dest dir exists
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    with open(dest, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"Processed {src} to {dest}")

process_file(r"c:\MacBookLinux\streamflix\app\src\main\java\com\streamflixreborn\streamflix\utils\TMDb3.kt",
             r"c:\MacBookLinux\streamflix-linux\core\src\main\kotlin\com\streamflixreborn\streamflix\utils\TMDb3.kt")
             
process_file(r"c:\MacBookLinux\streamflix\app\src\main\java\com\streamflixreborn\streamflix\utils\TmdbUtils.kt",
             r"c:\MacBookLinux\streamflix-linux\core\src\main\kotlin\com\streamflixreborn\streamflix\utils\TmdbUtils.kt")
