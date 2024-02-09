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
Imagem com os endpoints no Swagger:

URL para acessar o Swagger:

```bash
localhost:8080/swagger-ui.html
```

![image](https://github.com/pauloruszel/api-gen-barcode/assets/12766450/02043cf8-2101-4000-a585-382c01991147)


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
