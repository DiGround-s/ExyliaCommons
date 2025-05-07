# ExyliaCommons

Biblioteca compartida para los plugins de Exylia que proporciona utilidades comunes y herramientas para el desarrollo de plugins para Minecraft.

## Características

- **ConfigManager**: Gestión de configuraciones y archivos YAML.
- **ColorUtils**: Utilidades para manejo de colores, componentes de texto y mensajes.
- **GradientUtils**: Aplicación de gradientes y colores hexadecimales.
- **DebugUtils**: Herramientas para depuración y mensajes en consola.
- **AnsiComponentLogger**: Soporte para colores ANSI en la consola.

## Instalación

### Usando Gradle

Añade el repositorio de Maven local (o el repositorio donde publiques la biblioteca) a tu `build.gradle`:

```gradle
repositories {
    mavenLocal() // Si has instalado la biblioteca localmente
    // O tu repositorio personalizado
}

dependencies {
    implementation 'net.exylia:exyliacommons:1.0.0'
}
```

### Usando Maven

Añade la dependencia a tu `pom.xml`:

```xml
<dependency>
    <groupId>net.exylia</groupId>
    <artifactId>exyliacommons</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Uso

### Configuración en tu plugin

```java
import net.exylia.commons.ExyliaCommons;
import net.exylia.commons.config.ConfigManager;
import net.exylia.commons.utils.DebugUtils;

public class MiPlugin extends JavaPlugin {
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Establecer el prefijo para DebugUtils
        DebugUtils.setPrefix(getName());
        
        // Mostrar mensaje de carga
        DebugUtils.sendPluginMOTD(getName());
        
        // Inicializar el ConfigManager
        List<String> configFiles = Arrays.asList("config", "messages");
        configManager = new ConfigManager(this, configFiles);
        
        // Mostrar mensaje de carga de ExyliaCommons
        ExyliaCommons.logInfo();
    }
}
```

### Mensajes con colores

```java
import net.exylia.commons.utils.ColorUtils;
import net.exylia.commons.utils.DebugUtils;

// Enviar mensaje a un jugador
Player player = getServer().getPlayer("Notch");
ColorUtils.sendPlayerMessage(player, "&aHola &b¿cómo estás?");

// Mostrar mensaje en consola
DebugUtils.logInfo("Plugin cargado correctamente");

// Usar gradientes
ColorUtils.sendPlayerMessage(player, "<#FF0000>Este texto tiene un <#FF0000>gradiente</#00FF00> de colores");
```

### Manejo de configuraciones

```java
import net.exylia.commons.config.ConfigManager;

// Obtener un mensaje
String welcomeMessage = configManager.getMessage("welcome");

// Obtener una configuración
boolean featureEnabled = configManager.getConfig("config").getBoolean("features.myFeature");

// Recargar configuraciones
configManager.reloadAllConfigs();
```

## Compilación

Para compilar la biblioteca, usa:

```bash
./gradlew shadowJar
```

Esto generará un archivo JAR en la carpeta `build/libs/` que puedes usar en tus plugins.

## Instalación en repositorio local

Para instalar la biblioteca en tu repositorio local de Maven:

```bash
./gradlew publishToMavenLocal
```

## Licencia

Este proyecto está bajo la licencia MIT.