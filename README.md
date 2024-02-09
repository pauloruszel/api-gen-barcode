# 🎯 API QR Code
Este é um projeto de API RESTful para geração de QR Codes e gerenciamento de convidados para eventos. Com ele, é possível criar QR Codes personalizados com diferentes tamanhos, cores e opções de download ou visualização. Além disso, oferece funcionalidades para cadastrar e confirmar a presença de convidados através de QR Codes únicos.

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
URL: /convidados/cadastrar
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
URL: /convidados/confirmar/{idUnico}
```
Descrição: Confirma a presença de um convidado através do ID único fornecido pelo QR Code.

Exemplo para Confirmar Convidado
```bash
localhost:8080/convidados/confirmar/e5b87a8c-6ed4-4995-b272-66d3d9dc5ba9
```

## 🛠️ Execução do projeto
Para executar o projeto, é necessário ter o Java 21 e o Maven instalados, além de Docker para a conteinerização.

## 🚀 Como usar
## Clone o repositório:
git clone https://github.com/pauloruszel/api-gen-barcode.git

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

## Testes unitários
Para rodar os testes unitários, execute o comando abaixo:
```bash
mvn test
```

## Observações:
A aplicação permite a geração de QR Codes personalizados, incluindo opções para definir o tamanho, as cores do primeiro plano e do fundo, e se a imagem deve ser baixada ou visualizada no navegador. Também é possível retornar a imagem em formato base64.
