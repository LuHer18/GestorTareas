# Security Notes

## Firebase Realtime Database rules

This repository includes `database.rules.json` with deny-by-default rules. The app stores tasks under `tasks/{uid}/{taskId}`, and reads/writes are only allowed when the authenticated user's UID matches `{uid}`.

To apply and verify the rules, use the Firebase CLI from this repository after selecting the correct Firebase project:

```bash
firebase use <project-id>
firebase database:rules:get
firebase deploy --only database
```

## Android backup

`android:allowBackup="false"` is set in `app/src/main/AndroidManifest.xml` to avoid backing up local app/session data. The XML backup rule files also exclude app data domains for consistency.

## API keys

Do not commit local secrets or generated Firebase configuration changes. Keep `local.properties` and `app/google-services.json` out of review changes unless intentionally rotating configuration.

In Google Cloud Console, restrict API keys by Android app package name and SHA-1 certificate fingerprint, and restrict each key to only the APIs it needs.

## Local checks

Useful verification commands:

```bash
./gradlew lint
./gradlew test
```
