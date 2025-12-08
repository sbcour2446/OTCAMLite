# Introduction 
OTCAM is a blanket program that currently contains two applications: ICDL and 3DVis. OTCAM is owned, developed, and maintained by OTC contractors and is continually updated to meet evolving test requirements.

ICDL enables different LVC systems to interoperate through a common TENA interface, connecting Live RTCA systems (HITS, XLCC, FlexTrain) with simulation systems (OneSAF, ExCIS, MUSE). It synchronizes simulated effects onto real players and provides data collection for post-event analysis.

3DVis provides 3D map visualization in both live and post-event modes. It integrates with ICDL to present the Common Operating Picture (COP) and supports replay, timeline control, RTCA interaction, and synchronized visualization across LVC logs, videos, JBCP messages, TIRs, surveys, and custom overlays such as LIDAR, alerts, UAS metadata, and CSV-based data.

![Alt text](./OTCAM_Architecture.png)

# Getting Started
This section describes how to install required tools, authenticate Git, clone the OTCAM Lite repository, build the software, and run 3DVis.

## AVD Software Install
From the GPU AVD Software Center:

1. Install OpenJDK 21  
2. Install Git  
3. Install VSCode  

---

## Create Azure Repo PAT

1. Login to Azure DevOps:  
   https://devops.cloud.army.mil/ATEC%20Data%20Mesh/OTC  
2. Click your User Icon (top right)  
3. Select Security  
4. Select New Token  
5. Create a token (Full Access for now)  
6. Save the PAT to a .txt file  

---

## Add PAT Credentials to Git

Open PowerShell and run the following commands, replacing **yourPAT** with the actual PAT:

$MyPat = "yourPAT"
$B64Pat = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes(":$MyPat"))
git config --global --remove http.https://devops.cloud.army.mil
git config --global --add http.https://devops.cloud.army.mil.extraHeader "Authorization: Basic $B64Pat"

---

## Clone Code Repo

1. Open VSCode  
2. Select “Clone Git Repository”  
3. Paste this URL:

https://devops.cloud.army.mil/ATEC%20Data%20Mesh/OTC/_git/OTCAM%20Lite

4. Open the cloned folder in VSCode  

---

# Build and Test

## Build OTCAM

In the VSCode terminal:

.\gradlew clean deployAll

This creates the full distribution in:

build\dist\

---

# Run OTCAM

From the distribution folder:

cd build\dist
.\jre\bin\java.exe -DTENA_PLATFORM="w10-vs2019-64" -DTENA_VERSION="6.0.8" -jar ".\3DVis-2.0.2.20.jar"

---

# Contribute

Contributions should follow OTC development practices, including:

- Branching  
- Pull Requests  
- Code Review  
- Versioned Builds  

For README guidance:  
https://docs.microsoft.com/en-us/azure/devops/repos/git/create-a-readme

Example READMEs:  
https://github.com/aspnet/Home  
https://github.com/Microsoft/vscode  
https://github.com/Microsoft/ChakraCore  