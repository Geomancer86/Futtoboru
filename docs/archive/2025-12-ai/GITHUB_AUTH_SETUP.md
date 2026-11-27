# GitHub Authentication Setup (2FA)

## Problem
With 2FA enabled, Git can't use your password. You need a **Personal Access Token (PAT)**.

## Solution: Create and Use Personal Access Token

### Step 1: Create Personal Access Token

1. Go to: https://github.com/settings/tokens
2. Click **"Generate new token"** → **"Generate new token (classic)"**
3. Name it: `Futtoboru Git Push`
4. Select scope: **`repo`** (full control of private repositories)
5. Click **"Generate token"**
6. **COPY THE TOKEN** (you won't see it again!)

### Step 2: Use Token for Push

**Option A: Windows Credential Manager (Recommended)**

1. Open **Windows Credential Manager**:
   - Press `Win + R`
   - Type: `control /name Microsoft.CredentialManager`
   - Or search "Credential Manager" in Start Menu

2. Go to **Windows Credentials** tab

3. Find `git:https://github.com` entry
   - If it exists: Click → Edit → Update password with your **PAT**
   - If it doesn't exist: Click "Add a generic credential"
     - Internet or network address: `git:https://github.com`
     - User name: `Geomancer86` (your GitHub username)
     - Password: **Paste your PAT here**

4. Try push again: `git push origin develop`

**Option B: Use Token in URL (Temporary)**

```bash
git remote set-url origin https://YOUR_TOKEN@github.com/Geomancer86/futtoboru.git
git push origin develop
```

**Option C: Use SSH Instead**

```bash
# Generate SSH key if you don't have one
ssh-keygen -t ed25519 -C "your_email@example.com"

# Add to GitHub: https://github.com/settings/keys

# Change remote to SSH
git remote set-url origin git@github.com:Geomancer86/futtoboru.git

# Push
git push origin develop
```

## Quick Test

After setting up, test with:
```bash
git push origin develop
```

---

*Note: PAT tokens are like passwords - keep them secret!*

