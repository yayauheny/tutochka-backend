import os
import re

for root, _, files in os.walk('backend/src/main/kotlin'):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # Remove @Serializable and its import
            content = content.replace('@Serializable\n', '')
            content = content.replace('import kotlinx.serialization.Serializable\n', '')
            
            # Replace @SerialName with @JsonProperty
            content = content.replace('@SerialName', '@JsonProperty')
            content = content.replace('import kotlinx.serialization.SerialName', 'import com.fasterxml.jackson.annotation.JsonProperty')
            
            # Replace kotlinx JsonObject/JsonElement with JsonNode
            content = content.replace('import kotlinx.serialization.json.JsonObject', 'import com.fasterxml.jackson.databind.JsonNode')
            content = content.replace('import kotlinx.serialization.json.JsonElement', 'import com.fasterxml.jackson.databind.JsonNode')
            content = content.replace('JsonObject', 'JsonNode')
            content = content.replace('JsonElement', 'JsonNode')
            
            if 'JsonNode' in content and 'import com.fasterxml.jackson.databind.JsonNode' not in content:
                content = re.sub(r'package (.*?)\n', r'package \1\n\nimport com.fasterxml.jackson.databind.JsonNode\n', content, count=1)
                
            if '@JsonProperty' in content and 'import com.fasterxml.jackson.annotation.JsonProperty' not in content:
                content = re.sub(r'package (.*?)\n', r'package \1\n\nimport com.fasterxml.jackson.annotation.JsonProperty\n', content, count=1)

            with open(path, 'w') as f:
                f.write(content)
