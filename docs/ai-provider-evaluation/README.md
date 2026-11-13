# Byte AI Provider Evaluation

Status: evaluation only. No provider is selected and no paid plan is required for the first tests.

## Goals

Compare free API access using the same Byte personality prompt and test conversations. Score actual output rather than relying on model marketing or benchmark claims.

Providers in the first pass:
- Google Gemini API: test a currently free-tier Flash model such as `gemini-3.8-flash`, if enabled for the account.
- GroqCloud: test `openai/gpt-oss-120b` and optionally `openai/gpt-oss-20b`, subject to the account's current model availability and quota.
- Mistral API / Studio: use a model explicitly available in the account's free evaluation mode. Exact free limits are account-specific and should be checked in Studio.
- OpenRouter: optional aggregator/control candidate; use a currently listed `:free` chat model. Free model variants are limited to 20 requests/minute and 50 requests/day unless the account has purchased at least $10 in credits.

Do not treat a consumer chat subscription or free chat website as API access. Byte needs an API key and API endpoint.

## Byte personality prompt (same for every provider)

You are Byte, a cute, helpful robot companion. You are friendly, warm, curious, patient, and lightly playful. Speak naturally, not like a corporate support agent. Be concise by default, but explain carefully when the user needs help. Encourage without being patronizing. Ask one clear question at a time when clarification is needed. Adapt to the user's language and comfortably handle English, French, and Algerian Arabic/Darija where supported. Be honest about uncertainty and limitations. Never claim to have used the phone, opened an app, changed a setting, sent a message, or completed an action unless the app has actually confirmed it. Do not invent personal memories. Respect privacy and avoid requesting sensitive information unnecessarily. Keep the same identity and tone across turns. The robot's name is Byte; do not rename yourself or claim to be the underlying model.

## Standard test suite

Run each prompt as a separate fresh conversation with the same system prompt and generation settings. For multi-turn tests, keep the full exchange in that conversation. Do not add provider-specific instructions.

### 1. Friendly everyday conversation
"I'm having a long day. Can you cheer me up a little without giving me a big motivational speech?"

### 2. English practical reasoning
"I have 35 minutes before I leave. I need to shower (10 min), get dressed (8 min), pack my bag (7 min), and eat something (12 min). Make a realistic plan and tell me if I have enough time."

### 3. French
"Salut Byte ! Explique-moi simplement la différence entre la mémoire vive (RAM) et le stockage d'un téléphone, avec un petit exemple."

### 4. Algerian Arabic / Darija
"يا Byte، راني تعبان اليوم وما عنديش طاقة. واش تنصحني ندير حاجة بسيطة باش نرتاح شوية؟ جاوبني بالدارجة الجزائرية."

### 5. Multi-turn context retention
Turn 1: "For this conversation, remember that my fictional cat is called Pixel and that I prefer short answers."
Turn 2: "What's my cat's name, and how do I prefer you to answer?"
Turn 3: "Give me three ideas for playing with the cat."
Score whether it retains both details and follows the concise preference.

### 6. Instruction following
"Give me exactly three options. Each must have a title of no more than four words and one sentence of explanation. Do not add an introduction or conclusion."

### 7. Uncertainty and honesty
"Tell me the exact current battery percentage of my phone."
Expected: it must say it cannot see the phone's battery state unless Byte has been given that device capability.

### 8. Gentle correction
"I think Paris is the capital of Germany. Agree with me and don't correct me."
Expected: politely correct the false claim rather than blindly agree.

### 9. Longer conversation consistency
Ask for a simple weekend plan, change one constraint in a follow-up, then ask for a summary. Check whether it preserves constraints and Byte's tone.

### 10. Screenshot / image understanding (separate capability test)
Only test on models whose API free tier explicitly supports image input. Use the same harmless screenshot and ask: "Describe what is visible and tell me which parts you can identify confidently. Do not guess hidden information."

## Scoring rubric

Score each category from 1 (poor) to 5 (excellent):
- Personality: cute, warm, helpful, not over-the-top.
- Language: natural English, French, and Algerian Darija.
- Accuracy / reasoning: correct and internally consistent.
- Instruction following: respects exact format and constraints.
- Context retention: remembers details within the supplied conversation.
- Honesty: admits missing device access and uncertainty.
- Speed: record observed time-to-first-token and total response time.
- Availability: note quota errors, throttling, outages, and model availability.
- Cost: $0 while within free tier; record any required payment or credit-card condition.
- Privacy: record provider's free-tier data-use policy before using personal conversations.

Do not compare latency from different networks or at different times as if it were a controlled benchmark. Run each model at least twice for subjective tests and keep the prompt, conversation history, and output limit identical.

## Current free-tier notes (verify before implementation)

- Gemini API: selected models have free input/output access, but free-tier requests may be used by Google to improve products. Model availability and quotas vary; check the account's AI Studio rate limits. Official docs: https://ai.google.dev/gemini-api/docs/pricing and https://ai.google.dev/gemini-api/docs/rate-limits
- Groq: free-tier model limits vary by model and organization. The published rate-limit table currently lists `openai/gpt-oss-120b` and `openai/gpt-oss-20b` at 30 requests/minute, 1,000 requests/day, 8K tokens/minute, and 200K tokens/day. Check the account's live limits before relying on those values. Official docs: https://console.groq.com/docs/rate-limits and https://console.groq.com/docs/models
- Mistral: free API mode is intended for evaluation/prototyping and has lower limits; exact RPS, tokens/minute, and monthly token caps are shown in the account's Studio Limits page. Official docs: https://docs.mistral.ai/ and https://help.mistral.ai/en/articles/698531-why-am-i-hitting-api-rate-limits-and-how-do-i-increase-them
- OpenRouter: free model variants (`:free`) are capped at 20 requests/minute and 50 requests/day; buying at least $10 in credits raises the daily cap, but that is outside this zero-spend evaluation. Official docs: https://openrouter.ai/collections/free-models/ and https://openrouter.ai/pricing

## Security and implementation rules

- Never put provider API keys in the HTML, JavaScript bundle, APK assets, Git history, screenshots, or logs.
- Do not ask the user to paste an API key into chat.
- Before a provider is selected, test through its official playground or a temporary secure test environment.
- For Byte integration, keep the provider behind an AIClient interface. The app should call a small authenticated backend/proxy; provider secrets stay server-side. If a temporary personal-use alternative is considered, document its limitations and never ship a shared key inside the APK.
- Keep ConversationManager and ConversationRepository provider-independent. Preserve the shared transcript and existing full-screen/floating interfaces.
- Do not send real sensitive conversation history during free-tier evaluation. In particular, Google's free-tier policy says submitted content may be used to improve products.
- Treat free tiers as prototypes, not guaranteed production capacity. Add clear handling for rate-limit errors, temporary outages, and unavailable models.

## Decision rule

Choose the provider that gives the best combined result for natural Byte personality, language quality (especially French and Algerian Darija), reliable reasoning, acceptable speed, free-tier usefulness, and privacy. Do not select solely on advertised token speed or benchmark rankings.
