<a href="https://dei.tecnico.ulisboa.pt/"><img style="float: right;" src="res/logodei.png"></a>

### [Cloud Computing and Virtualization 2023/2024](https://fenix.tecnico.ulisboa.pt/disciplinas/AVExe23/2023-2024/2-semestre) ([MEIC-A](https://fenix.tecnico.ulisboa.pt/cursos/meic-a)/[MEIC-T](https://fenix.tecnico.ulisboa.pt/meic-t), [METI](https://fenix.tecnico.ulisboa.pt/merc), [MECD](https://fenix.tecnico.ulisboa.pt/cursos/mecd))

&nbsp;
&nbsp;
&nbsp;
&nbsp;

# Welcome to Cloud Computing and Virtualization Labs!

---

## Overview

The goal of these lab classes is to experiment and interact with different virtualization and cloud computing concepts. In particular, we will focus on code instrumentation using Pin and Javassist, and on two cloud computing virtualization technologies: Infrastructure-as-a-Service and Function-as-a-Service.

To complete this part of the course, you will be required to develop a project, and to present a research paper (more details below).

Labs will be organized according to the following structure, updated weekly (tutorials for each lab are published on the Friday before the lab class):

---
- Week 1 (April 15-19)
    - [Introduction to CNV Labs](README.md);
    - [Introduction to Pin - Intel Instrumentation Tool](labs/lab-pin/README.md);


---
- Preparation for Week 2 (April 22-26)
### AWS Account Setup

To use AWS you will need an AWS Free-tier account in order to be able to access and allocate resources. 

- Register at [https://aws.amazon.com/free](https://aws.amazon.com/free);

- When asked for a credit card, use an MBNET/MBWAY (from your e-banking or mbway.pt) or some other virtual card with a low balance (e.g., 5 EUR) to register (AWS may try to check if the card is real by creating a temporary transaction of 1 USD);

- Follow this [walkthough](https://gitlab.rnl.tecnico.ulisboa.pt/cnv/cnv24/-/blob/master/labs/lab-aws/res/create-verify-aws-free-tier-account.pdf) to create and verify your AWS Free-tier account;

- When using AWS, two **important!** notes to keep in mind:

    - **Use only Free Tier resources!**

    - **Do not leave any resources running after you are finished with your work session!** If you forget a VM running, you may use all your credit!

---



- Week 2 (April 22-26)
    - [Introduction to AWS - Amazon Web Services](labs/lab-aws/README.md);
---

- Week 3 (April 29-May 3)
    - [Introduction to Javassist - Java Bytecode Instrumentation Tool](labs/lab-javassist/README.md);


- Week 4 (May 6-10)
    - This week: project support;
    - [Additional content on DynamoDB 
    that will be used as the metrics storage service (MSS) in AWS (only required after the checkpoint).](https://gitlab.rnl.tecnico.ulisboa.pt/cnv/cnv24/-/blob/master/labs/lab-aws/README_dynamodb.md)


- Week 5 (May 13-17)
    - [Introduction to Function-as-a-Service](labs/lab-faas/README.md);
   - Paper presentations;
    <!--
    - Checkpoint evaluation and feedback;
    -->


## Labs and Project

**Students should form groups of 3 students as soon as possible. Check the instructions posted in the initial announcement in the course page and sent to all enrolled students via email.**


## Paper presentations

Students will be required to present a research paper during one of the lab classes. Each group (i.e., project group) should pick up to 4 papers (ordered by preference), from the [proposed set](https://gitlab.rnl.tecnico.ulisboa.pt/cnv/cnv24/-/blob/master/papers/). Within the same shift, paper presentations cannot be repeated. There are more papers than groups per shift to increase choice alaternatives.


Each presentation will take 15 minutes followed by questions. 
We will keep an up-to-date [schedule](https://docs.google.com/spreadsheets/d/1LHJuOwjoHBzm6ETywe1Mzf2s93ISe-9MTpOxEo7sPio/edit?usp=sharing) as you send us your paper preferences.


Paper presentations will take place in Weeks 5 and 6 and 7 to account for holidays in days of lab (if needed, presentations of that shift will be rearranged around other shifts on a group-basis, or in special schedules, e.g., during office hours. Students not presenting can attend presentations either in other shifts or in the special schedules).


**Send us your paper preferences to meic-cnv@tecnico.ulisboa.pt after your group is enrolled and after you have looked over through the papers.**

**After completing the presentations, send us your slidesets (.ppt(x) or .pdf) to the course email at: meic-cnv@tecnico.ulisboa.pt.**


## Introduction to Git

All the course material will be made available through Git. It is therefore crucial that you are confortable using it. Make sure that you are familiar with the following concepts: `commit`, `push`, `pull`, `clone`, `fork`.

There are many good online tutorials on how to use git. Here are just two examples:
- [gittutorial](https://git-scm.com/docs/gittutorial)
- [git - the simple guide](https://rogerdudler.github.io/git-guide/)

All students will be given read permissions for the course repository. For the project, students will also use a Git repository. We will automatically create such repositories for you.

## CNV Linux Environment

We provide a light and optimized VM with all the software necessary to run CNV labs and project. This VM is based on Vagrant and VirtualBox. Please follow the [tutorial](vm/README.md) on how to setup the VM.

CNV requires a Linux development environment. We strongly recommend using the VM we provide. However, if that is not an option for you, there are several other alternatives:
- use lab computers (even if through remote connection, i.e., SSH);
- build your own Linux Virtual Machine;
- use the [Linux Subsystem](https://docs.microsoft.com/en-us/windows/wsl/install) (Windows users);
- install the software manually (for Linux and Mac users).

**Make sure you have a Linux environment as soon as possible.**

<!--
## Contacts and Lab Office Hours

For any questions, please send an email to [meic-cnv@tecnico.ulisboa.pt](mailto:meic-cnv@tecnico.ulisboa.pt).

In alternative, there are also office hours:

- Tue 08h00 to 11h00, Alameda, INESC-ID 604;
- Wed 11h30 to 13h00, Tagus, 2-N3.15;
- Thu 08h00 to 11h00, Alameda, INESC-ID 604;
- Fri 11h30 to 13h00, Tagus, 2-N3.15;

Zoom link: https://videoconf-colibri.zoom.us/j/9056821599?pwd=d3JjYXQyY05iQ2RNYWVTN3VzSTJndz09

-->
