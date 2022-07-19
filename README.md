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
- AGENDAR <LOCAL> : indicando um local onde pretende ser agendada a vacinação; 
- DESMARCAR : cancela um agendamento anterior e permite fazer um novo. 



