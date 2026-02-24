# 📚 Duda — App de Leitura de Ebooks

App Android nativo em Kotlin para leitura de ebooks com vocabulário, categorias e sistema de destaques.

---

## 📋 Stack técnica

| Item | Versão |
|---|---|
| Kotlin | 1.9.24 |
| Android Gradle Plugin | 8.3.2 |
| Gradle Wrapper | 8.6 |
| JDK | 17 |
| compileSdk / targetSdk | 34 |
| minSdk | 26 (Android 8.0) |
| UI | Jetpack Compose (BOM 2024.05.00) |
| Arquitetura | Clean Architecture |
| Banco de dados | Room 2.6.1 |
| DI | Hilt 2.51.1 |
| Rede | Retrofit 2.11.0 + OkHttp |
| Imagens | Coil 2.6.0 |

---

## 🗂️ Estrutura do projeto

```
com.duda.app/
├── data/
│   ├── local/          → Room: Entities, DAOs, Database, Migrations
│   ├── remote/         → Retrofit: API, DTOs
│   └── repository/     → Implementações dos repositórios
├── domain/
│   ├── model/          → Entidades de domínio
│   ├── repository/     → Interfaces dos repositórios
│   └── usecase/        → Casos de uso
├── presentation/
│   ├── library/        → Biblioteca (grid + carrossel)
│   ├── reader/         → Leitor (PDF, EPUB, TXT, HTML)
│   ├── vocabulary/     → Vocabulário
│   ├── bookdetail/     → Detalhes do livro
│   ├── importbook/     → Importação de arquivos
│   ├── settings/       → Configurações
│   └── common/         → Tema, MainScreen, componentes reutilizáveis
└── core/
    ├── di/             → Módulos Hilt
    ├── navigation/     → NavGraph + Screen routes
    └── util/           → FileUtils, Extensions
```

---

## 🚀 Configurando o Codemagic

### 1. Conectar o repositório

1. Acesse [codemagic.io](https://codemagic.io)
2. Clique em **"Add application"**
3. Selecione **GitHub** e autorize o acesso
4. Escolha o repositório `duda`
5. Selecione **"Flutter App"** → depois troque para **"Other"** → o Codemagic detectará o `codemagic.yaml`

### 2. ⚠️ OBRIGATÓRIO: commitar o Gradle Wrapper

O `gradle-wrapper.jar` é um arquivo binário que **não está incluído** neste repositório por padrão. Você precisa gerá-lo **uma vez** na sua máquina local:

```bash
# Na raiz do projeto:
gradle wrapper --gradle-version 8.6

# Confirmar os arquivos gerados:
ls -la gradle/wrapper/
# Deve mostrar: gradle-wrapper.jar e gradle-wrapper.properties

# Commitar tudo:
git add gradle/wrapper/gradle-wrapper.jar gradlew gradlew.bat
git commit -m "chore: add Gradle Wrapper 8.6"
git push origin main
```

> **Por que é obrigatório?** O Codemagic usa o Gradle Wrapper para baixar a versão exata do Gradle (8.6). Sem o `.jar`, o build falha com `Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain`.

### 3. Criar Variable Group (opcional para debug)

Para o workflow de **Debug**, não há variáveis obrigatórias. Mas se quiser receber notificação por email:

1. No Codemagic, vá em **"Teams"** → **"Shared environment variables"**
2. Crie um grupo chamado `duda_env`
3. Adicione a variável:
   - `CM_BUILD_NOTIFICATION_EMAIL` = seu@email.com

Se não quiser notificações, remova as últimas linhas do `publishing` no `codemagic.yaml`.

### 4. Disparar o primeiro build

O build dispara automaticamente em qualquer `push` para a branch `main`. Ou você pode clicar em **"Start new build"** no painel do Codemagic.

---

## 📦 Artefatos gerados

Após o build, os artefatos ficam disponíveis na aba **"Artifacts"** do build no Codemagic:

- `app-debug.apk` — APK de debug, pronto para instalar diretamente
- Relatórios de build (se houver erros de lint)

---

## 🔏 Configurando Release (para o futuro)

Quando quiser gerar o Release AAB para a Play Store:

### Passo 1: Gerar keystore

```bash
keytool -genkey -v \
  -keystore duda-release.jks \
  -alias duda \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### Passo 2: Configurar no Codemagic

1. No Codemagic, vá em **"Teams"** → **"Code signing identities"** → **"Android keystores"**
2. Faça upload do arquivo `duda-release.jks`
3. Anote o nome que você deu (ex: `duda_keystore`)

### Passo 3: Criar Variable Group para Release

Crie um grupo chamado `duda_release` com as variáveis:

| Variável | Valor |
|---|---|
| `CM_KEYSTORE_PASSWORD` | senha do keystore |
| `CM_KEY_ALIAS` | `duda` (ou o alias que você escolheu) |
| `CM_KEY_PASSWORD` | senha da chave |

### Passo 4: Ativar o workflow Release

No `codemagic.yaml`, descomente o bloco `android-release` (remova os `#`).

No `app/build.gradle.kts`, descomente o bloco `signingConfigs` e `signingConfig = signingConfigs.getByName("release")`.

---

## 🔧 Build local (desenvolvimento)

```bash
# Clone o repositório
git clone https://github.com/SEU_USUARIO/duda.git
cd duda

# Gerar Gradle Wrapper (apenas uma vez)
gradle wrapper --gradle-version 8.6

# Build debug
./gradlew assembleDebug

# O APK estará em:
# app/build/outputs/apk/debug/app-debug.apk

# Instalar no dispositivo conectado
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🐛 Troubleshooting — Erros mais comuns

### ❌ `Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain`

**Causa:** `gradle-wrapper.jar` não foi commitado.

**Solução:**
```bash
gradle wrapper --gradle-version 8.6
git add gradle/wrapper/gradle-wrapper.jar
git commit -m "fix: add missing gradle-wrapper.jar"
git push
```

---

### ❌ `SDK location not found`

**Causa:** Variável `ANDROID_HOME` ou `ANDROID_SDK_ROOT` não configurada.

**Solução no Codemagic:** O Codemagic configura automaticamente. Se ocorrer localmente:
```bash
# No seu ~/.bashrc ou ~/.zshrc
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

---

### ❌ `Execution failed for task ':app:compileDebugKotlin'` — Erro de compilação

**Causa:** Geralmente erro de sintaxe ou importação incorreta.

**Solução:**
```bash
./gradlew assembleDebug --stacktrace 2>&1 | grep -A 20 "error:"
```

---

### ❌ `Could not resolve com.google.dagger:hilt-android:2.51.1`

**Causa:** Repositório Maven não disponível ou problema de rede.

**Solução:** Verificar se `settings.gradle.kts` tem `google()` e `mavenCentral()` no bloco `repositories`. No Codemagic, aguardar e tentar um novo build.

---

### ❌ `KSP: Room schema export directory is not provided`

**Causa:** Room exige um diretório para exportar o schema do banco.

**Solução:** Adicionar ao `app/build.gradle.kts`:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

---

### ❌ Build demora mais de 60 minutos

**Causa:** Cache não está funcionando ou projeto muito grande.

**Solução:** Verificar se `GRADLE_OPTS` contém `-Dorg.gradle.caching=true` no `codemagic.yaml`.

---

### ❌ `chmod: gradlew: No such file or directory`

**Causa:** `gradlew` não foi commitado.

**Solução:**
```bash
git add gradlew gradlew.bat
git commit -m "fix: add gradlew scripts"
git push
```

---

## ✅ Smoke Test Manual (10 passos)

Execute estes passos após instalar o APK gerado pelo Codemagic:

1. **Instalação** — Instalar o APK em dispositivo físico com Android 8.0+ (API 26+)
2. **Launch** — Abrir o app → Tela Biblioteca carrega sem crash, mostra estado vazio
3. **Import PDF** — Tocar no botão `+` → Importar 1 arquivo PDF → Aparece na Biblioteca
4. **Import EPUB** — Importar 1 arquivo EPUB → Aparece na Biblioteca
5. **Share Intent** — Compartilhar um arquivo TXT de outro app para o Duda → Importação completa
6. **Leitor** — Tocar em um livro → Leitor abre → Swipe horizontal muda a página
7. **Vocabulário** — No leitor, segurar uma palavra → Tocar "Buscar significado" → Definição exibida → Palavra salva na aba Vocabulário
8. **Highlight** — Selecionar trecho → Tocar no ícone de bookmark → Escolher cor → Highlight salvo
9. **Categoria** — Ir em Detalhes do livro (long press) → Alterar categoria para "Lido" → Verificar na aba Categorias
10. **Progresso** — Avançar algumas páginas → Fechar o app completamente → Reabrir → Leitor retoma na última página

---

## 📝 Adicionando migrations do banco

Quando mudar o schema do banco (adicionar coluna, tabela, etc.):

1. Incrementar `version` em `AppDatabase.kt`
2. Adicionar migration em `Migrations.kt`
3. Registrar em `AppDatabase.ALL_MIGRATIONS`
4. Registrar em `DatabaseModule` (`.addMigrations(...)`)

Exemplo:
```kotlin
// Migrations.kt
val migration1To2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE books ADD COLUMN language TEXT NOT NULL DEFAULT 'pt'")
    }
}
```

---

## 📄 Licença

Projeto privado — todos os direitos reservados.
