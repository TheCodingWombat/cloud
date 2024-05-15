<a href="https://dei.tecnico.ulisboa.pt/"><img style="float: right;" src="../res/logodei.png"></a>

### [Cloud Computing and Virtualization 2023/2024](https://fenix.tecnico.ulisboa.pt/disciplinas/AVExe23/2023-2024/2-semestre) ([MEIC-A](https://fenix.tecnico.ulisboa.pt/cursos/meic-a)/[MEIC-T](https://fenix.tecnico.ulisboa.pt/meic-t), [METI](https://fenix.tecnico.ulisboa.pt/merc), [MECD](https://fenix.tecnico.ulisboa.pt/cursos/mecd))

&nbsp;
&nbsp;
&nbsp;
&nbsp;

# CNV Virtual Machine Setup

---

## Overview

These guide will help to configure your laptop or desktop properly for the your work in the CNV Course. It will setup an optimized (smaller, hence faster) virtual machine bundled with only the tools required for the CNV course. The instructions are generally suitable for host machines running the following Operating Systems (i.e. your already existing OS):
- Microsoft Windows from version 10 up.
- Apple macOS from versions 10.13 ’High Sierra’ up.
- Debian-based Linux, such as Ubuntu (recommended) from versions 16.04 LTS up.

---

## Preliminary Set Up of the Host Environment

This section describes the minimum setup of the CNV virtualized environment for systems running Windows, macOS and Linux. We will use the following tools or programs: 
- **Vagrant**: is an open-source product for building and maintaining portable virtual development environments.
- **VirtualBox**: Oracle VM VirtualBox is a free and open-source hosted hypervisor for x86 virtualization.

### Installation For Windows 10 systems

For Virtualbox, go to the [Downloads page](https://www.virtualbox.org/wiki/Downloads) and select the Windows hosts latest binaries. The Oracle VM VirtualBox installation can be started by double-clicking on the executable file you downloaded, and then follow the instructions from the installer dialog. If you wish to use VirtualBox on Windows, you must ensure that Hyper-V is not enabled. You can also disable it by going through the Windows system settings:

1) Right click on the Windows button and select 'Apps and Features';
2) Select Turn Windows Features on or off;
3) Unselect Hyper-V and click OK.

You might have to reboot your machine for the changes to take effect

For Vagrant, go to the [Downloads page](https://www.vagrantup.com/downloads) and select the WINDOWS installer of the latest version of Vagrant for your system.

### Installation for macOS systems

For Virtualbox, go to the [Downloads page](https://www.virtualbox.org/wiki/Downloads) and select the OS X hosts latest binaries. For Mac OS X hosts, Oracle VM VirtualBox ships in a dmg disk image file. Perform the following steps to install on a Mac OS X host:

1) Double-click on the dmg file, to mount the contents;
2) A window opens, prompting you to double-click on the VirtualBox.pkg installer file displayed in that window;
3) This starts the installer, which enables you to select where to install Oracle VM VirtualBox;
4) An Oracle VM VirtualBox icon is added to the Applications folder in the Finder.

For Vagrant, go to the [Downloads page](https://www.vagrantup.com/downloads) and select the MAC OS X installer of the latest version of Vagrant for your system.

### Installation for Linux systems

For Virtualbox, go to the [Downloads page](https://www.virtualbox.org/wiki/Downloads) and then the Linux distribution page and there follow the instructions for your Linux distribution.

For Vagrant, go to the [Downloads page](https://www.vagrantup.com/downloads) and select the LINUX (or DEBIAN) installer of the latest version of Vagrant for your system.

## Creating and using the Virtual Machine for the Lab

Having the host environment prepared, it is now time to create the VM! If you have ever tried to create Virtual Machines used for testing through a Graphical User Interface (GUI), you know that it can be a painful and very manual process (installing the Operating System, the packages/applications, configure them, etc.) or downloading an already created VM with several GB of size that takes a huge amount of time to download. There is also a tendency to leave forgotten VMs around in your computer (when running, consuming precious resources, when shutdown still using up space) for a long time.

Vagrant eliminates much of that extra labour, as most of the process is completely automated.

To get started you need to position the creation of the VM in an adequate working folder. It is recommended that the work directory (folder) does not have a name with spaces and/or accented characters, and that specifically in Windows, not placed under the user's HOME folder. Therefore, use, for example:
- in Windows `D:\cnv\`
- in macOS or Linux `/cnv/`

There download the following files: [cnv-provision.sh](cnv-provision.sh), and [Vagrantfile](Vagrantfile) which will allow a smooth setup of the system.

To start the VM, open a Terminal (in Linux or macOS) or a Powershell console in Windows (if command prompt does not work) and go to the working folder. In there, start the VM with the following command (equivalent to Power Up or Start):

`vagrant up`

The first time you start the VM it will in fact build it, and so it may take up a few minutes, depending on the speed of the host system and of your Internet connection. When finished the VM is booted and you can establish sessions with the VM using the following command (secure shell, without needing username or password):

`vagrant ssh`

For convenience, you can issue this command in as many shell/windows/sessions as you want so that you can have multiple windows logged to your VM. The session is established and you will get the machine prompt similar to the following:

<pre><code>
Welcome to Ubuntu 18.04.5 LTS (GNU/Linux 4.15.0-136-generic x86_64)
 
vagrant@cnvlab:~$
</pre></code>

When you have finished your work, and in order to exit the session with the VM use the command `exit`:

<pre><code>
vagrant@cnvlab:~$ exit
logout
Connection to 127.0.0.1 closed.
</pre></code>

In order to stop the Virtual Machine and to verify the global state of all active VM environments (managed by Vagrant) on the system, you can use the following commands (the first is equivalent to a shutdown):

`vagrant halt`

`vagrant global-status`

The last command allows you to confirm that the statuses of the VMs is powered off. The next time you want to work with the VM again, then use the command (the VM will boot in less than a minute, typically):

`vagrant up`

### Shared Folders with the VM

The VM is prepared to have a shared folder structure with your host system. You will notice that when creating the VM for the first time, that a new folder with the name `cnv-shared` appeared in the working directory. That folder is also accessible inside the VM, and so, whatever you place in that folder is accessible from both your host machine and the VM.

With this feature, you will be able, for example, to edit your Code using your preferred Code Editor (e.g., VScode, Eclipse, Atom, etc.), and then, inside the VM just use/compile/run the code.