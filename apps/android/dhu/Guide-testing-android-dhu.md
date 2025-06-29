# Guide d'Installation d'APK sur Desktop Head Unit (DHU)

## À propos de ce guide

Ce guide détaille l'installation d'applications Android (fichiers APK) pour les tester sur **Desktop Head Unit (DHU)**, l'émulateur d'écran de voiture Android Auto développé par Google. DHU permet de simuler l'expérience Android Auto sur un ordinateur de développement.

## Prérequis techniques

### Configuration requise

- **Smartphone Android** : Version 9 (API 28) ou supérieure
- **Android Studio** avec SDK Manager installé
- **Ordinateur de développement** : Windows, macOS ou Linux
- **Câble USB** compatible pour connexion smartphone-ordinateur
- **Android Auto** installé sur le smartphone

### Vérification de compatibilité

| Composant | Version minimale | Recommandée |
|-----------|------------------|-------------|
| Android | 9.0 (API 28) | 14.0+ |
| Android Studio | 4.0+ | Dernière |
| DHU | 2.0+ | Dernière |

## Installation et configuration DHU

### 1. Installation du DHU

1. **Ouvrir Android Studio**
2. **Accéder au SDK Manager** (Tools → SDK Manager)
3. **Onglet SDK Tools** → Cocher **"Android Auto Desktop Head Unit Emulator"**
4. **Télécharger** et installer le package

Le DHU s'installe dans : `SDK_LOCATION/extras/google/auto/`

### 2. Configuration Linux/macOS

Sur Linux ou macOS, rendre le binaire exécutable :

```bash
chmod +x ./desktop-head-unit
```

#### Configuration Linux supplémentaire

Pour DHU 2.x sur Linux, installer les dépendances :

```bash
# Vérifier version GLIBC (minimum 2.32)
ldd --version

# Installation des bibliothèques (Debian/Ubuntu)
sudo apt-get install libc++1 libc++abi1
```

## Préparation du smartphone Android

### Activation du mode développeur Android Auto

1. **Ouvrir Android Auto** sur le smartphone
2. **Appuyer 10 fois** sur le titre "Android Auto" dans la barre d'outils
3. **Confirmer** l'activation du mode développeur
4. **Accéder aux paramètres** Android Auto
5. **Onglet "Développeur"** → Activer les options nécessaires

### Configuration des sources inconnues

#### Android 8.0 et versions ultérieures

1. **Paramètres** → **Applications**
2. **Menu** (trois points) → **Accès spécial**
3. **Installation d'applications inconnues**
4. **Sélectionner l'application source** (navigateur, gestionnaire fichiers)
5. **Activer** "Autoriser depuis cette source"

#### Versions Android antérieures

1. **Paramètres** → **Sécurité**
2. **Sources inconnues** → **Activer**
3. **Confirmer** l'activation

## Installation d'APK pour Android Auto

### Méthode 1 : Installation directe

1. **Télécharger l'APK** compatible Android Auto
2. **Ouvrir** le fichier APK depuis le gestionnaire de fichiers
3. **Confirmer l'installation** et accepter les permissions
4. **Vérifier** que l'app apparaît dans la liste des applications

### Méthode 2 : Via ADB (Advanced)

```bash
# Connecter le smartphone via USB
adb devices

# Installer l'APK
adb install chemin/vers/application.apk

# Vérifier l'installation
adb shell pm list packages | grep nom_package
```

### Applications courantes Android Auto

| Application | Fonction | Compatibilité |
|-------------|----------|---------------|
| Carstream | YouTube sur Android Auto | Modifiée |
| Fermata Auto | Streaming IPTV | Compatible |
| Screen2Auto | Mirroring d'écran | Compatible |
| AA Mirror | Partage d'écran | Compatible |

## Lancement et test DHU

### Démarrage du DHU

1. **Connecter le smartphone** via USB à l'ordinateur
2. **Activer le serveur head unit** dans Android Auto
3. **Terminal/Invite de commande** :

```bash
# Configuration du tunnel ADB
adb forward tcp:5277 tcp:5277

# Naviguer vers le dossier DHU
cd SDK_LOCATION/extras/google/auto/

# Lancer DHU (Linux/macOS)
./desktop-head-unit

# Lancer DHU (Windows)
desktop-head-unit.exe
```

### Vérification du fonctionnement

1. **Interface DHU** s'affiche sur l'ordinateur
2. **Smartphone** entre en mode Android Auto
3. **Applications installées** apparaissent dans l'interface DHU
4. **Tester** les fonctionnalités de navigation et interaction

## Résolution de problèmes

### Erreurs courantes

**Problème** : DHU ne se connecte pas

- **Solution** : Vérifier `adb forward tcp:5277 tcp:5277`
- **Vérifier** que le mode développeur Android Auto est activé

**Problème** : APK ne s'installe pas

- **Solution** : Vérifier les sources inconnues activées
- **Contrôler** la compatibilité Android Auto de l'APK

**Problème** : Application non visible dans DHU

- **Solution** : Redémarrer Android Auto et DHU
- **Vérifier** que l'app supporte Android Auto

### Logs et diagnostic

```bash
# Afficher les logs Android Auto
adb logcat | grep -i "auto\|car"

# Vérifier les applications installées
adb shell pm list packages -3
```

## Considérations de sécurité

### Bonnes pratiques

- **Désactiver les sources inconnues** après installation
- **Vérifier la provenance** des fichiers APK
- **Scanner** les APK avec un antivirus
- **Utiliser uniquement** des applications de confiance

### Risques à éviter

- APK de sources non fiables
- Applications demandant des permissions excessives
- Modifications système non autorisées
- Installation en mode root sans précautions

## Applications spécialisées Android Auto

### AAAD (Android Auto Apps Downloader)

**AAAD** facilite l'installation d'applications modifiées pour Android Auto :

1. **Installer AAAD** depuis une source fiable
2. **Sélectionner** l'application souhaitée
3. **Installation automatique** sans root requis
4. **Configuration** dans Android Auto

### Applications de développement

Pour les développeurs d'applications Android Auto :

- **Android Auto Media Browser** (déprécié)
- **Android Auto Messaging Simulator** (déprécié)
- **DHU** (recommandé pour tests)

## Maintenance et mise à jour

### Mise à jour du DHU

1. **SDK Manager** → Vérifier les mises à jour
2. **Télécharger** la nouvelle version DHU
3. **Redémarrer** Android Studio si nécessaire

### Gestion des applications

- **Désinstaller** : `adb uninstall nom.package.application`
- **Mettre à jour** : Installer la nouvelle version APK
- **Sauvegarder** : `adb backup nom.package.application`

## Support et ressources

### Documentation officielle

- [Guide officiel DHU](https://developer.android.com/training/cars/testing/dhu)
- [Documentation Android Auto](https://developer.android.com/training/cars)
- [Développement d'apps Auto](https://developers.google.com/cars/design)

### Communauté

- Forums Android Developers
- GitHub projets Android Auto
- Stack Overflow (tag: android-auto)

---

## Notes importantes

> **Avertissement** : L'installation d'APK depuis des sources inconnues peut présenter des risques de sécurité. Utilisez uniquement des applications de confiance et désactivez les sources inconnues après installation.

> **Développeurs** : DHU est un outil de développement. Pour la distribution d'applications, suivez les guidelines officielle Android Auto et Google Play Store.

---

*Ce guide est destiné aux développeurs et testeurs d'applications Android Auto. Il doit être utilisé dans un environnement de développement sécurisé.*

M
