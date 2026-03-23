# IAgo - Deploy GitHub Pages

## URL

`https://aniversarioyago.github.io/IAgo/`

## Configuração única no GitHub

1. Abra `https://github.com/aniversarioyago/IAgo`.
2. Vá em `Settings -> Pages`.
3. Source: `Deploy from a branch`.
4. Branch: `main`.
5. Folder: `/docs`.
6. Save.

## Deploy (automatizado)

```bash
cd /home/kayque/Repos/IAgo
chmod +x deploy_github_pages.sh
./deploy_github_pages.sh
```

O script faz:

- build web com `:composeApp:jsBrowserDistribution`
- sincroniza artefatos para `docs/`
- cria `docs/.nojekyll`
- faz `git commit` e `git push` em `main`

## Backend remoto

Para web e Android em dispositivos reais, o backend deve estar em HTTPS público.

Padrão atual do frontend web:

- `https://iago-backend.azurewebsites.net`

Para usar outro backend no deploy:

```bash
./deploy_github_pages.sh "https://seu-backend.com"
```

## Verificação rápida

```bash
curl -i https://aniversarioyago.github.io/IAgo/
curl -i https://iago-backend.azurewebsites.net/health
curl -i -X POST https://iago-backend.azurewebsites.net/api/chat -H "Content-Type: application/json" -d '{"message":"Oi"}'
```

## Troubleshooting

- Se abrir README: confirme `main` + `/docs` no Pages.
- Se chat falhar: backend remoto está fora ou sem `GEMINI_API_KEY`.
- Se Android falhar em rede local: use backend HTTPS remoto, não `localhost`.
