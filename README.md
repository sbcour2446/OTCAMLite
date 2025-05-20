# Introduction 
OTCAM is a blanket program that currently contains two applications:  ICDL and 3DVis.  OTCAM is owned, developed and maintained by OTC contractors.  It is constantly being modified to meet different test requirements.   

 

ICDL is an application that provides a way for different LVC systems to interoperate via a common TENA interface.  It can provide an interface between Live RTCA systems like HITS, XLCC and Flextrain and simulation systems like OneSAF, ExCIS and MUSE.  It allows simulated fire missions to have effects on real players and allows real players location and health to be seen by simulations.  It has built-in data collection abilities that can be utilized in post-event analysis in 3DVis.

 

3DVis is an application that provides data visualization on a 3D map, both live and post-event.  Live, it interfaces with ICDL and provides the current Common Operating Picture (COP) while allowing the user to back up in time and replay past events.  It can offer some interactive control over the RTCA status of live players.  Post-event, it provides the same 3D map while replaying and time-synchronizing the events from multiple data sources including LVC data from the OTCAM database, Video captures, JBCP messaging and manual data including TIRs and surveys.  Custom displays can be developed per test.  These have included LIDAR scans and results, system alerts, csv locations, UAS metadata, and data logs.           

![Alt text](./OTCAM_Architecture.png)

# Getting Started
TODO: Guide users through getting your code up and running on their own system. In this section you can talk about:
1.	Installation process
2.	Software dependencies
3.	Latest releases
4.	API references
Test Text

test

# Build and Test
TODO: Describe and show how to build your code and run the tests. 

# Contribute
TODO: Explain how other users and developers can contribute to make your code better. 

If you want to learn more about creating good readme files then refer the following [guidelines](https://docs.microsoft.com/en-us/azure/devops/repos/git/create-a-readme?view=azure-devops). You can also seek inspiration from the below readme files:
- [ASP.NET Core](https://github.com/aspnet/Home)
- [Visual Studio Code](https://github.com/Microsoft/vscode)
- [Chakra Core](https://github.com/Microsoft/ChakraCore)