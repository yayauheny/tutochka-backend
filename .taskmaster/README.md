# Task Master Setup

## Quick Start

```bash
# 1. Copy templates
cp .taskmaster/templates/config-template.json .taskmaster/config.json
# Edit PROJECT_NAME and USER_ID

# 2. Configure API keys in .cursor/mcp.json
# Add PERPLEXITY_API_KEY and GOOGLE_API_KEY

# 3. Initialize
task-master init
```

## Automatic Workflow

Every implementation request automatically:
1. Creates PRD in `.taskmaster/docs/`
2. Parses PRD to generate subtasks
3. Works on generated subtasks
4. Logs progress in subtask details

## Local-First Strategy

- Read `.taskmaster/tasks/tasks.json` for all read operations
- Cache task context in memory
- Use API calls only for writes

## Templates

- `config-template.json` - Task Master configuration
- `agent-rules-template.mdc` - Agent integration rules
- `workflow-template.mdc` - Workflow template

## Recovery

All context stored locally:
- `.taskmaster/tasks/tasks.json` - Complete task structure
- Task `details` field - Full implementation logs
- `.taskmaster/docs/research/` - Research results

Recovery works without API calls.
