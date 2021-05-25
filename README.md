# Enunciado do trabalho - Desenvolvimento de um servidor que permite agendar vacinações.

-Cada cliente deve identificar-se indicando o seu número do SNS (o servidor possui informação prévia de quais os números válidos). 
-De seguida o cliente pode indicar vários comandos alternativos: 
## LOCAIS : para receber uma lista de locais de vacinação; 
## AGENDAR [LOCAL]: indicando um local onde pretende ser agendada a vacinação; 
## DESMARCAR : libertando  um agendamento anterior e podendo fazer um novo. 
### O servidor deve tentar fazer uma gestão dos locais e horas de agendamento no sentido de agendar na vaga mais próxima face ao local pretendido pelo cliente.

Devem ter em atenção o controlo de concorrência e tentar permitir agendamentos concorrentes desde que em locais distintos.
Como programa cliente pode utilizar o "nc" ou, se preferir, desenvolver o seu próprio    programa.

Notas:

- Cada máquina cliente sabe o IP e Porto do servidor.
- Devem utilizar primitivas de programação com sockets, programação concorrente
e controlo de concorrência local sempre que necessário.
- Deve ser usada a linguagem de programação Java.
- Os grupos podem ser de 4 participantes.
- A submissão do trabalho (até às 13h00 do dia 2 de Junho) consta de um .zip
ou .tgz com o código fonte e um relatório .pdf de até 6 páginas.
- No dia da apresentação, 4 de Junho, haverá uma sessão de 10 minutos
(cada grupo) em que se mostra as escolhas feitas e principais desafios e
características da solução. Será também demonstrado o programa em execução.