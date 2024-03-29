[EN]

# Development of a scheduling server.

Notes:

- Java programming language.
- Programming primitives with sockets, concurrent programming and local concurrency control whenever necessary.
- Each client machine knows the IP and Port of the server.
- As a client program you can use "nc" or, if you prefer, develop your own program.

Objectives:

- Each client must identify himself by indicating his SNS number (the server has prior information on which numbers are valid).
- The server must try to manage the places and times of scheduling in order to schedule in the closest vacancy in relation to the place intended by the customer.
- The system must pay attention to concurrency control and try to allow concurrent bookings as long as they are in different locations.

Command List:

- LOCAIS : to receive a list of available vaccination locations;
- AGENDAR [ LOCAL ] : indicating a place where the vaccination is to be scheduled;
- DESMARCAR : cancels a previous appointment and allows you to make a new one.

------------------------------------------------------------------------------------------------------------------------------------------------------------------
[PT]

# Desenvolvimento de um servidor de agendamento.

Notas:

- Linguagem de programação Java.
- Primitivas de programação com sockets, programação concorrente e controlo de concorrência local sempre que necessário.
- Cada máquina cliente sabe o IP e Porta do servidor.
- Como programa cliente pode utilizar o "nc" ou, se preferir, desenvolver o seu próprio programa.

Objetivo:

- Cada cliente deve identificar-se indicando o seu número do SNS (o servidor possui informação prévia de quais os números válidos). 
- O servidor deve tentar fazer uma gestão dos locais e horas de agendamento no sentido de agendar na vaga mais próxima face ao local pretendido pelo cliente.
- O sistema deve ter em atenção o controlo de concorrência e tentar permitir agendamentos concorrentes desde que em locais distintos.

Lista de Comandos:

- LOCAIS : para receber uma lista de locais de vacinação; 
- AGENDAR [ LOCAL ] : indicando um local onde pretende ser agendada a vacinação; 
- DESMARCAR : cancela um agendamento anterior e permite fazer um novo. 



