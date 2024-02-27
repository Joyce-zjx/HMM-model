# HMM-model

In this project, I implement my own HMM model in order to classify the malware data(given 3 different types of data: winwebsec, zbot and zeroaccess) by using Java language.
First, I chose the first 80% of data from zbot folder for HMM model training, then I chose the rest 20% data from zbot, and the same amount of data from zeroaccess for testing.
After finishing the data pre processing, I found out that the datasets we applied have 420 types of symbol(opcode), which means M = 420. After training and testing, I obtained the corresponding score of two malware families. I have attached a report of the result analysis.
