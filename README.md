# Thindesk · Dashboard administrativo

Requer JDK 21, MongoDB em execução e acesso ao Maven Central no primeiro build.

No PowerShell, na pasta do projeto:

```powershell
$env:THINDESK_PASSWORD = 'escolha-uma-senha-local'
$env:MONGODB_URI = 'mongodb://localhost:27017/thindesk'
.\mvnw.cmd spring-boot:run
```

Abra http://localhost:8080/admin/dashboard e entre com o usuário **admin** e a senha definida no terminal. Para usar um cluster remoto, defina MONGODB_URI com a conexão correspondente. Mantenha a senha e a URI com credenciais fora do Git.

Para inserir seis chamados fictícios em um banco local de demonstração, execute em outro terminal:

```powershell
mongosh "mongodb://localhost:27017/thindesk" --file scripts/dados-demo.js
```

O script insere ou regrava somente os seis identificadores da demonstração. Em um banco com registros reais, os indicadores incluem todos os chamados compatíveis com a consulta.

- [Documentação da atividade](docs/atividade.md)
- [Roteiro de demonstração](docs/roteiro-video.md)
