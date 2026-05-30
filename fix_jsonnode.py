import os

for root, _, files in os.walk('common/src/main/kotlin'):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            content = content.replace('JsonNode', 'Map<String, Any>')
            content = content.replace('import com.fasterxml.jackson.Map<String, Any>', '')
            content = content.replace('import com.fasterxml.jackson.databind.Map<String, Any>', '')
            content = content.replace('import kotlinx.serialization.json.JsonElement', '')
            
            with open(path, 'w') as f:
                f.write(content)
