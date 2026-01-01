# Push Library to GitHub

## Steps to Push to GitHub

### Step 1: Create a new repository on GitHub

1. Go to https://github.com/darshanadsw
2. Click the "+" icon in the top right corner
3. Select "New repository"
4. Name it: `configurable-logger` (or your preferred name)
5. Choose if it should be Public or Private
6. **DO NOT** initialize with README, .gitignore, or license (we already have these)
7. Click "Create repository"

### Step 2: Add files and make initial commit

```bash
cd /Users/darshana/Documents/personal-projects-2026/configurable-logger

# Add all files
git add .

# Make initial commit
git commit -m "Initial commit: Configurable Logger library"
```

### Step 3: Add remote and push

```bash
# Add your GitHub repository as remote (replace YOUR_REPO_NAME if different)
git remote add origin https://github.com/darshanadsw/configurable-logger.git

# Or if using SSH (if you have SSH keys set up):
# git remote add origin git@github.com:darshanadsw/configurable-logger.git

# Push to GitHub
git push -u origin master
```

### Alternative: If you prefer 'main' branch

```bash
# Rename branch to main
git branch -M main

# Push to GitHub
git push -u origin main
```

## What to include/exclude

The `.gitignore` file is already configured to exclude:
- `target/` directory (compiled classes)
- `.idea/` (IntelliJ IDEA files)
- `.mvn/wrapper/maven-wrapper.jar`
- Other build artifacts

## Repository Name Suggestion

Common names for this type of library:
- `configurable-logger`
- `spring-configurable-logger`
- `method-logger`
- `dynamic-logger`

Choose what works best for you!

