# BlockChain-CryptoCurrency-AtyponTraining
## Description
The final project submission for Atypon 2018 Summer Internship - Section 9, the project was to build a block-chain system and use it to build a CryptoCurrency system supporting multiple contributors.

Here's a link to the project [Project Prerequisites](Project_Prerequisites.pdf).

## Details
### The project was divided into two main parts:
1. The main server for the blockchain. [Blockchain](Blockchain):
	- The application runs on a peer2peer network, but to avoid the complexity in network discovery (Not the intention of the project), the addresses of the users are stored in a known server for new users to collect addresses of other users the first time they are joining the network.
2. The end user application. [BlockchainMainServer](BlockchainMainServer)
	- The application used by the end user, where all the information about all contributors are held and where the transactions are made and mined.
3. The reports of the project. [Reports](Reports)
	- The reports included: how **"Clean Code Principles"** are satisfied in the code, what **"Data structure"** were used, what **"Design Patterns"** were used, how **"Effective Java"** points are satisfied in the code and how the code satisfied the **"SOLID Principles"**.
