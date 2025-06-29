# Procédure d'Installation d'APK sur Crosscall Core-Z5 pour Tests MIB2 (via Android Auto)

Ce guide détaille la procédure d'installation d'applications Android (fichiers APK) à partir de sources inconnues sur un smartphone Crosscall Core-Z5, en vue de les tester sur des véhicules équipés du système MIB2 via Android Auto.

**Note Importante:** Le système MIB2 est un système d'infodivertissement automobile. Il n'est pas conçu pour installer et exécuter directement des applications Android arbitraires. Le processus de test d'applications sur MIB2 se fait généralement via la projection d'écran (comme Android Auto ou CarPlay), où l'application s'exécute sur le smartphone et est affichée sur l'écran du véhicule. Ce guide se concentre sur cette méthode.

## 1. Préparation du Crosscall Core-Z5

### 1.1 Activation des sources inconnues

Pour installer des APK qui ne proviennent pas du Google Play Store, vous devez activer l'option "Installer des applications inconnues" sur votre Crosscall Core-Z5.

**Note:** Le numéro de série du Crosscall Core-Z5 ciblé pour cette application est `A75259FRCNAAPDV00FB`.

1.  Accédez aux **Paramètres** de votre Crosscall Core-Z5.
2.  Naviguez vers **Applications** (ou "Applications & notifications").
3.  Recherchez et sélectionnez **Accès spécial** (cela peut varier légèrement selon la version d'Android, par exemple "Installer des applications inconnues" directement).
4.  Identifiez l'application à partir de laquelle vous prévoyez d'installer l'APK (par exemple, votre navigateur web comme Chrome, ou votre gestionnaire de fichiers).
5.  Activez l'option **"Autoriser depuis cette source"** pour cette application.

### 1.2 Transfert de l'APK vers le Crosscall Core-Z5

Vous pouvez transférer le fichier APK vers votre Crosscall Core-Z5 de plusieurs manières :

*   **Via USB:** Connectez votre téléphone à votre ordinateur via un câble USB et copiez le fichier APK dans un dossier accessible sur le téléphone (par exemple, le dossier "Téléchargements").
*   **Via Téléchargement Direct:** Si vous avez accès à internet sur le téléphone, téléchargez l'APK directement depuis une source fiable via le navigateur web du téléphone.
*   **Via Bluetooth/Cloud:** Utilisez Bluetooth ou un service de stockage cloud (Google Drive, Dropbox, etc.) pour transférer le fichier.

## 2. Installation de l'APK sur le Crosscall Core-Z5

1.  Ouvrez un **gestionnaire de fichiers** sur votre Crosscall Core-Z5 (ou l'application à partir de laquelle vous avez autorisé les sources inconnues).
2.  Naviguez jusqu'à l'emplacement où vous avez transféré le fichier APK.
3.  Appuyez sur le fichier APK pour lancer l'installation.
4.  Un avertissement de sécurité peut apparaître ; confirmez que vous souhaitez installer l'application.
5.  Suivez les instructions à l'écran pour terminer l'installation.

## 3. Préparation pour le Test sur MIB2 (via Android Auto)

Le MIB2 est compatible avec Android Auto. Pour tester votre application, elle doit être compatible avec Android Auto.

### 3.1 Vérification de la compatibilité Android Auto de l'application

Assurez-vous que l'application que vous souhaitez tester est conçue pour Android Auto. Les applications Android Auto doivent respecter des directives spécifiques de conception et de développement pour fonctionner correctement dans l'environnement automobile.

### 3.2 Activation du mode développeur Android Auto sur le Crosscall Core-Z5

1.  Ouvrez l'application **Android Auto** sur votre Crosscall Core-Z5.
2.  Appuyez plusieurs fois (environ 10 fois) sur le titre "Android Auto" dans la barre d'outils de l'application.
3.  Confirmez l'activation du mode développeur lorsque la boîte de dialogue apparaît.
4.  Accédez aux **Paramètres** d'Android Auto.
5.  Dans l'onglet **"Options pour les développeurs"** (ou similaire), activez les options nécessaires pour le débogage et le test.

### 3.3 Connexion au Véhicule MIB2

1.  Connectez votre Crosscall Core-Z5 au port USB de votre véhicule MIB2.
2.  Sur l'écran du véhicule (MIB2), sélectionnez l'option **Android Auto**.
3.  Votre Crosscall Core-Z5 devrait passer en mode Android Auto, et l'interface Android Auto devrait s'afficher sur l'écran MIB2.
4.  Si votre application est compatible avec Android Auto, elle devrait apparaître dans le lanceur d'applications d'Android Auto sur l'écran MIB2.

## 4. Test et Dépannage

*   **Lancement de l'application:** Lancez votre application depuis l'interface Android Auto sur l'écran MIB2.
*   **Vérification des logs:** Si vous rencontrez des problèmes, utilisez `adb logcat` sur votre ordinateur connecté au Crosscall Core-Z5 pour diagnostiquer les erreurs.

    ```bash
    adb logcat | grep -i "AndroidAuto\|your_app_package_name"
    ```

*   **DHU (Desktop Head Unit):** Pour des tests plus approfondis ou si vous n'avez pas accès au véhicule, utilisez le DHU sur votre ordinateur pour simuler l'environnement Android Auto. Le guide `dhu/Guide-testing-android-dhu.md` fournit des instructions détaillées sur l'utilisation du DHU.

---

**Avertissement de sécurité:** L'installation d'applications à partir de sources inconnues présente des risques de sécurité. Assurez-vous toujours de la fiabilité de la source de l'APK avant de l'installer.
