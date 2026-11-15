# 🎯 API QR Code
Este projeto consiste em uma API RESTful versátil projetada para a geração de QR Codes personalizáveis e o gerenciamento eficiente de convidados para eventos diversos. Através desta API, usuários podem criar QR Codes adaptáveis, permitindo ajustes em tamanho, cor, e oferecendo alternativas entre download direto e visualização online.

Além de sua funcionalidade principal de geração de QR Codes, a API se estende para facilitar o gerenciamento de convidados, incorporando recursos para o cadastro e a confirmação de presença de convidados via QR Codes únicos. Esses QR Codes podem ser utilizados para uma variedade de propósitos, incluindo, mas não se limitando a, controle de acesso a eventos, validação de identidade e facilitação no gerenciamento de registros de presença.

Implementada com uma arquitetura baseada em microserviços, esta API adota as melhores práticas de desenvolvimento, incluindo separação de responsabilidades através da distinção entre camadas de Controller e Service, injeção de dependências para maior flexibilidade e manutenibilidade do código, e o uso de padrões reativos com Spring WebFlux para lidar com eventos de maneira não bloqueante, garantindo assim um desempenho superior em ambientes com alto volume de requisições.

Este projeto é ideal para organizadores de eventos, sistemas de gestão de acesso, e desenvolvedores que buscam integrar funcionalidades de geração de QR Code e gerenciamento de convidados de forma eficiente e escalável em suas aplicações.

## Padrões de microserviço utilizados
* Separação em camadas (Controller, Service)
* Injeção de dependências com Spring
* Uso de Record para representação de dados
* Reativo com Spring WebFlux

## ✔️ Tecnologias e bibliotecas usadas
- `Java 21`
- `Spring Boot 3.2.2`
- `Spring WebFlux`
- `Lombok`
- `ZXing`
- `PostgreSQL` com R2DBC para reatividade no acesso ao banco de dados
- `Docker` para conteinerização

## Endpoints
### Gerar QR Code

- Método: `GET`

```bash
URL: /api/v1/qrcode?texto={texto}&scale={scale}&foreground={foreground}&background={background}&download={download}&base64={base64}
```
Descrição: Gera um QR Code com as opções especificadas.

Parâmetros
- `texto:` O texto que será codificado no QR Code.
- `scale:` A escala (tamanho) do QR Code.
- `foreground:` A cor do primeiro plano do QR Code.
- `download:` A cor do plano de fundo do QR Code.
- `download:` Opção para download do QR Code (valores possíveis: "true", "false").
- `base64:` Opção para retornar o QR Code em formato base64 (valores possíveis: "true", "false").

### Exemplo de teste
Você pode testar o endpoint com o seguinte payload:
```json
{
  "texto": "https://www.reddit.com",
  "scale": 200,
  "foreground": "#9C27B0",
  "background": "#E1BEE7",
  "download": "false",
  "base64": "false"
}
```

### Cadastrar Convidado
- Método: `POST`

```bash
URL: localhost:8080/api/v1/convidados/cadastrar
```
Descrição: Cadastra um convidado e gera um QR Code único para confirmação de presença.

Exemplo de Payload para Cadastro de Convidado
```json
{
  "nome": "Fulano de Tal",
  "email": "fulano@example.com"
}
```
Nota: Os campos idUnico, status e qrCode são gerados automaticamente pelo sistema e não devem ser incluídos no payload de envio.

## Confirmar Presença
- Método: `POST`

```bash
URL: localhost:8080/api/v1/convidados/confirmar/{idUnico}
```
Descrição: Confirma a presença de um convidado através do ID único fornecido pelo QR Code.

Exemplo para Confirmar Convidado
```bash
localhost:8080/api/v1/convidados/confirmar/e5b87a8c-6ed4-4995-b272-66d3d9dc5ba9
```
Imagem com os endpoints no Swagger:

URL para acessar o Swagger:

```bash
localhost:8080/swagger-ui.html
```

![image](https://github.com/pauloruszel/api-gen-barcode/assets/12766450/85d34995-9e91-4125-a8bf-92aea8010482)


## 🛠️ Execução do projeto
Para executar o projeto, é necessário ter o Java 21 e o Maven instalados, além de Docker para a conteinerização.

## 🚀 Como usar
## Clone o repositório:
```bash
git clone https://github.com/pauloruszel/api-gen-barcode.git
```
## 📁 Entre na pasta do projeto:
```bash
cd api-gen-barcode
```
### Execute o comando abaixo para compilar e empacotar o projeto:
```bash
mvn clean package
```
## 🐳 Execute o docker-compose:
```bash
docker-compose up --build
```
A API estará disponível em http://localhost:8080.

## Observações:
A aplicação permite a geração de QR Codes personalizados, incluindo opções para definir o tamanho, as cores do primeiro plano e do fundo, e se a imagem deve ser baixada ou visualizada no navegador. Também é possível retornar a imagem em formato base64.

## MVP de recepção

Requisitos locais: Java 21, Maven 3.9+, Node.js 24 e PostgreSQL. Os comandos abaixo
usam uma base local de desenvolvimento. A aplicação inicializa as tabelas.

```bash
export SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/casamento
export SPRING_R2DBC_USERNAME=seu_usuario
export SPRING_R2DBC_PASSWORD=sua_senha
export RECEPTION_ADMIN_KEY="$(openssl rand -hex 24)"

mvn test
mvn spring-boot:run
```

Em outro terminal, com a mesma variável `RECEPTION_ADMIN_KEY`:

```bash
cd frontend
npm ci
npm test
npm run build
npx playwright install chromium
npm run test:e2e
npm run dev
```

Abra o URL informado pelo Vite e use a chave do cerimonial para criar ou abrir um
evento. O Vite encaminha `/api` para a API (porta 8080 por padrão). Se usar outra
porta, defina `API_ORIGIN`. Para entrega ao piloto, sirva o frontend e a API na
mesma origem HTTPS, com fallback para `index.html` na rota `/recepcao`. A câmera
requer HTTPS nos aparelhos da equipe. O teste local no Chromium pode usar
`CHROMIUM_PATH` com o caminho para um executável já instalado.

Na revisão da planilha, use o CSV exportado com IDs para preservar a identidade
dos convidados. A troca de grupo transfere a pessoa para o QR do novo convite.
As remoções exigem confirmação na prévia e mantêm o histórico. A recepção
precisa de conexão para registrar chegadas. Em uma queda, use o CSV da
contingência, anote os horários e registre-os pelo painel quando voltar.

A chave de cerimonial é compartilhada no piloto e dá acesso a todos os eventos
da instalação. Use uma instalação de confiança por operação. Links da
recepção são limitados a um evento, expiram em 24 horas e identificam o operador
que registrou a chegada.
