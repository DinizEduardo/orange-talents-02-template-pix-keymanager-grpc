# Desafio: Pix

## Entendendo o desafio

Recentemente o Banco Central do Brasil (BCB) trouxe ao mercado o **Pix**. Em poucas palavras, o Pix é um novo meio de pagamentos para fazer transferências de forma rápida, sem esperar dias para que o pagamento “caia” na conta de quem o receberá ou ter que fazê-los só em dias da semana, no horário comercial. Além de poder transferir dinheiro para outras pessoas, será possível também fazer pagamentos a estabelecimentos usando o Pix, por exemplo.

Com o Pix, pagamentos e transferências são **concluídos em alguns segundos** e podem ser feitos a qualquer horário e dia, incluindo finais de semana e feriados. O Pix vai, portanto, facilitar e agilizar as transferências de valores entre pessoas e estabelecimentos comerciais, o pagamento de contas e até recolhimento de impostos e taxas de serviços, entre outras possibilidades.

Vale dizer que para enviar ou receber um Pix, não é necessário fazer nenhum cadastro ou baixar um aplicativo – ele pode ser usado diretamente no aplicativo de sua instituição; é necessário somente que ela ofereça esse meio de pagamento.

Dessa forma, nosso **objetivo** é garantir que nossos usuários possam:
1. efetuar pagamentos e transferências via Pix na nossa plataforma;
2. registrar suas chaves Pix em nossa plataforma usando suas informações pessoais: CPF, telefone celular, email ou uma chave aleatória. Com uma chave registrada, será possível receber e pagar via Pix;

Falando em microsserviços, nossos 4 serviços serão estes:

- **KeyManager-gRPC**: microsserviço responsável por fazer todo o gerenciamento das chaves Pix dos nossos clientes (usuários), além de ser o ponto central de comunicação da nossa arquitetura para busca de chaves;
- **KeyManager-REST**: microsserviço responsável por expor serviço KeyManager-gRPC através de uma API REST de tal forma que ela possa ser consumida pelo time de frontend de forma eficiente e segura;
- **Payment-gRPC**: microsserviço responsável por gerar cobranças Pix que serão compartilhadas via QR Code por nossos usuários; junto isso, haverá também a possibilidade de efetuar pagamentos a partir de um QR Code;
- **Payment-REST**: assim como o KeyManager-REST, esse microsserviço servirá de fachada para o time de frontend, ou seja, ele cuidará de expor uma API REST para os endpoints do microsserviço Payment-gRPC;

Em cada microsserviço precisamos ficar a atentos a requisitos não-funcionais como performance, escalabilidade, segurança e resiliência, além da qualidade e design do código produzido.

## Setup do Projeto - Key-Manager gRPC

### Objetivo

Sabemos que está ansioso(a) para começar a codificar, porém antes precisamos preparar nosso ambiente, portanto esse
será nosso objetivo nessa tarefa.

### Descrição

Nessa tarefa precisamos criar um projeto para atender as funcionalidades do **Key-Manager**, para tal, temos alguns
pré-requisitos de linguagem de programação e tecnologia, pois precisamos que esse projeto seja evoluído e mantido por
anos, portanto é extremamente importante a escolha das mesmas.

Nosso mais experiente membro do time, sugeriu os seguintes itens:

- Vamos escrever todo código com a linguagem de programação [Kotlin](https://kotlinlang.org/);
- Também vamos usar as tecnologias [Micronaut](http://micronaut.io/) e [gRPC](https://grpc.io/) para criar nossa API;
- E por fim, usaremos o [Gradle](https://gradle.org/) para rodar nosso build, testes e gerenciar nossas dependências;

### Hora de começar, por onde começar?

Existem várias maneiras de se começar um projeto com Micronaut, uma forma simples e bastante utilizada no mercado é via [Micronaut Launch!](https://micronaut.io/launch/)

* Não lembra como criar um projeto gRPC com Micronaut? Não seja por isso, os [primeiros minutos desse vídeo](https://www.youtube.com/watch?v=_53_sQp2bR4&feature=youtu.be) podem te ajudar;
* Não esqueça de [configurar seu IntelliJ](https://www.youtube.com/watch?v=dBXbbrG_UWU&feature=youtu.be) para seu projeto Micronaut;

### Resultado Esperado

Projeto gerado com as tecnologias sugeridas:

- [Kotlin](https://kotlinlang.org/)
- [Micronaut](http://micronaut.io/) e [gRPC](https://grpc.io/)
- [Gradle](https://gradle.org/)

## Registrando uma nova chave Pix

### Necessidades

Nosso usuário precisa registrar (cadastrar) uma nova chave Pix para que ele possa gerar cobranças e efetuar pagamentos de cobranças na nossa plataforma.

### Restrições

Para registrar uma chave Pix, precisamos que o usuário informe os seguintes dados:

- **Identificador do cliente** deve ser obrigatório:
    - Código interno do cliente na Instituição Financeira existente no [Sistema ERP do Itaú](http://localhost:9091/api/v1/private/contas/todas);

- **Tipo da chave** deve ser obrigatório, e pode ser:
    - CPF;
    - telefone celular;
    - email;
    - chave aleatória;

- **Valor da chave** deve ser válido e único com tamanho máximo de 77 caracteres:
    - Quando tipo for CPF, deve ser obrigatório e usar formato `^[0-9]{11}$` (por exemplo: `12345678901`);
    - Quando tipo for telefone celular, deve ser obrigatório e usar formato `^\+[1-9][0-9]\d{1,14}$` (por exemplo: `+5585988714077`);
    - Quando tipo for email, deve ser obrigatório e um endereço válido;
    - Quando tipo for chave aleatória, o valor da chave **não** deve ser preenchido pois o mesmo deve ser gerado pelo sistema no [formato UUID](https://en.wikipedia.org/wiki/Universally_unique_identifier);

- **Tipo de conta** associada a chave Pix deve ser obrigatória, e pode ser:
    - Conta Corrente;
    - Conta Poupança;

### Resultado Esperado

- Em caso de sucesso:
    - a chave Pix deve ser registrada e armazenada no sistema;
    - deve-se retornar um ID interno ("Pix ID") para representar a chave Pix criada pelo sistema;

- Em caso de chave já existente, deve-se retornar status de erro `ALREADY_EXISTS` com uma mensagem amigável para o usuário final;

- Em caso de erro, deve-se retornar o erro específico e amigável para o usuário final;

## Testando o Registro de chave Pix

### Necessidades

Nós finalizamos a implementação do endpoint responsável por [registrar uma nova chave Pix](005-registrando-uma-nova-chave-pix.md), mas a entrega ainda não está concluída. Precisamos testar a funcionalidade para garantir que ela funcionará em produção.

Eu sei, eu sei... você já testou sua aplicação de ponta a ponta, não é mesmo? A verdade é que embora tenhamos feito os testes com a ferramenta [BloomRPC](https://appimage.github.io/BloomRPC/) nós fizemos isso de forma totalmente **manual**. Mas será que foi suficiente?

Testes manuais são ótimos e super válidos no ciclo de desenvolvimento de um sofware, mas não dá para ignorar que eles são caros. O que estou querendo dizer é que qualquer alteração no código requer que façamos todos os testes novamente, pois somente assim será possível descobrir o impacto das mudanças. Enfim, muito trabalho para um(a) desenvolvedor(a), não é mesmo?

Some a isso o fato de que testes manuais são repetitivos e feitos por um ser humano, o que maximiza as chances de erros. Por esse motivo, vamos cobrir nosso código com **testes automatizados**, que nada mais são do que um programa que testa outro programa, desse modo sempre que fizermos alguma mudança no código basta rodarmos nossa bateria de testes. Portanto, **testes automatizados fazem parte da entrega** num time que preza pela qualidade de suas entregas.

Ter uma bateria de testes bem escrita e funcionando nos permite ter um ciclo de entrega mais curto e seguro, nos ajuda a encontrar e corrigir erros mais rapidamente; e claro, o desenvolvedor(a) terá mais confiança em modificar ou refatorar código, afinal se ele(a) cometer algum erro, por menor que seja, a bateria de testes vai alerta-lo(a) em questão de segundos.

Vamos escrever nosso primeiro teste?

### Restrições

Escrever testes automatizados para o endpoint gRPC de Registro de chave Pix implementado de tal forma que os testes garantam o que foi especificado na atividade.

Para guia-lo(a) nessa atividade, elencamos algumas restrições e pontos de atenção:

- favoreça a escrita de **testes de unidade** para lógicas de negócio que não fazem integração com serviços externos (banco de dados, APIs REST, mensageria, sistema de arquivos etc);
- favoreça a escrita de **testes de integração** para lógicas de negócio que conversam com serviços externos, como banco de dados, APIs REST etc;
- para tornar o teste mais próximo da produção, nos testes de integração **levante um servidor gRPC embarcado** e consuma os endpoints nos testes de integração;
- lembre-se de **testar os fluxos alternativos**, como cenários de erros do sistema ou entrada de dados inválida pelo usuário/serviço;
- favoreça o uso de um **banco de dados em memória** para facilitar a limpeza dos dados e simplificar o ambiente na sua pipeline de CI/CD;
- favoreça **mocks para chamadas à serviços externos**, como a API REST do Sistema ERP-ITAU e do Sistema Pix do BCB;
- fique sempre de olho na **cobertura do seu código**, especialmente nas branches de código, como `if`, `else`, `while`, `for`, `try-catch` etc;

### Resultado Esperado

O que esperamos ao final dessa atividade e que também consideramos importante:

- ter um percentual de cobertura de no mínimo **90% do código de produção**;
- ter coberto cenários felizes (happy-path) e fluxos alternativos;
- não precisar de instruções especiais para preparar o ambiente ou para rodar sua bateria de testes;
- sua bateria de testes deve rodar tanto na sua IDE quanto via **linha de comando**;
- que outro desenvolvedor(a) do time consiga rodar facilmente a bateria de testes do seu serviço;

## Removendo uma chave Pix existente

### Necessidades

Nosso usuário precisa excluir suas chaves Pix cadastradas, pois dessa forma, se necessário, ele poderá recriar uma chave associada à uma nova conta corrente ou poupança.

### Restrições

Para excluir uma chave Pix, precisamos que o usuário informe os seguintes dados:

- **Pix ID** (idenficiador interno da chave Pix) deve ser obrigatório;

- **Identificador do cliente** deve ser obrigatório:
  - Código interno do cliente na Instituição Financeira existente no [Sistema ERP do Itaú](http://localhost:9091/api/v1/private/contas/todas);

A chave pode ser removida somente pelo seu dono (cliente).

### Resultado Esperado

- Em caso de sucesso, a chave Pix deve ser excluída do sistema;

- Em caso de chave não encontrada, deve-se retornar status de erro `NOT_FOUND` com uma mensagem amigável para o usuário final;

- Em caso de erro, deve-se retornar o erro específico e amigável para o usuário final;