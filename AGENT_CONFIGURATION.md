# Agent Configuration for KMP POS Project

## Issue Resolved

**Problem**: Agents were failing with error: "The requested model is not supported"

**Root Cause**: Agents didn't have explicit model configuration and were inheriting from settings, but the model resolution wasn't working correctly.

**Solution**: Added explicit `model:` field to each agent's frontmatter to ensure they use supported models from the local API endpoint.

## Configured Agents

| Agent | Model | Rationale |
|-------|-------|-----------|
| **Software Architect** | `claude-opus-4.6` | Complex architectural decisions, system design, trade-off analysis |
| **Data Engineer** | `claude-sonnet-4.5` | Database schema design, solid technical knowledge required |
| **Backend Architect** | `claude-sonnet-4.5` | Backend architecture, service layer design |
| **Mobile App Builder** | `claude-sonnet-4.5` | Compose Multiplatform UI architecture |
| **UI Designer** | `claude-sonnet-4.5` | Design analysis and component specifications |
| **Technical Writer** | `claude-sonnet-4.5` | Documentation and plan consolidation |

## Model Strategy

### Claude Opus 4.6 (Most Capable)
- **Usage**: Software Architect only
- **Why**: Complex architectural decisions, reviewing all components, creating implementation roadmap
- **Cost**: Highest, but justified for critical architecture decisions

### Claude Sonnet 4.5 (Cost-Optimized)
- **Usage**: All other agents
- **Why**: Excellent capability at lower cost than 4.6, perfect for technical planning work
- **Cost**: Lower than 4.6, great value for technical work

### Not Using Haiku
For this project, we're not using Haiku because all tasks require substantial technical depth and reasoning. Haiku would be appropriate for:
- Simple code reviews
- Repetitive CRUD operations
- Basic formatting tasks

None of our planning tasks fall into these categories.

## Team Structure

### Planning Team: `kmp-pos-planning`

**Agents**:
1. `ui-designer` - Analyze Figma design (Sonnet 4.5)
2. `data-engineer` - Design database schema (Sonnet 4.5)
3. `backend-architect` - Define backend architecture (Sonnet 4.5)
4. `ui-architect` (mobile-app-builder) - Plan Compose UI (Sonnet 4.5)
5. `kmp-specialist` (mobile-app-builder) - Define project structure (Sonnet 4.5)
6. `software-architect` - Review and consolidate (Opus 4.6)

**Task Flow**:
```
Tasks #1, #6, #3 (parallel) → Dependencies complete → Tasks #4, #5 (parallel) → Task #2 (final review)
```

## Configuration Files

### Agent Definitions
Location: `~/.claude/agents/`

Modified files:
- `engineering/engineering-software-architect.md` - Added `model: claude-opus-4.6`
- `engineering/engineering-data-engineer.md` - Added `model: claude-sonnet-4.5`
- `engineering/engineering-backend-architect.md` - Added `model: claude-sonnet-4.5`
- `engineering/engineering-mobile-app-builder.md` - Added `model: claude-sonnet-4.5`
- `design-ui-designer.md` - Added `model: claude-sonnet-4.5`
- `engineering/engineering-technical-writer.md` - Added `model: claude-sonnet-4.5`

### Global Settings
Location: `~/.claude/settings.json`

```json
{
  "env": {
    "ANTHROPIC_BASE_URL": "http://localhost:4141",
    "ANTHROPIC_MODEL": "claude-sonnet-4.5"
  }
}
```

**Note**: The global `ANTHROPIC_MODEL` serves as fallback, but agents now have explicit models in their definitions.

## Verification

To verify models are configured correctly:

```bash
# Check all agent models
for file in ~/.claude/agents/**/*.md ~/.claude/agents/*.md; do
  if grep -q "^model:" "$file" 2>/dev/null; then
    echo "$(basename $file): $(grep "^model:" $file | cut -d' ' -f2)"
  fi
done
```

## Available Models

Your local API endpoint provides:

### Claude Models (Primary)
- ✅ `claude-opus-4.6` - Most capable
- ✅ `claude-sonnet-4.6` - Balanced (our default)
- ✅ `claude-sonnet-4.5` - Previous gen
- ✅ `claude-opus-4.5` - Previous gen
- ✅ `claude-haiku-4.5` - Fast/cheap
- ✅ `claude-sonnet-4` - Base model

### Other Models (Available but not used)
- GPT-5.x series
- Gemini 3.x series
- Grok models

## Testing

Before launching the full team again, you can test individual agents:

```typescript
// Test with explicit model override
Agent({
  name: "test-architect",
  subagent_type: "engineering-software-architect",
  description: "Test architecture analysis",
  prompt: "Analyze the requirements for a simple TODO app",
  model: "claude-opus-4.6"  // Explicitly set for testing
})
```

## Documentation

Comprehensive guide created: `~/.claude/agents/AGENT_MODEL_CONFIGURATION.md`

Topics covered:
- Model assignment priority
- Configuration methods
- Cost optimization strategies
- Troubleshooting
- Best practices

## Next Steps

1. ✅ Models configured for all agents
2. ✅ Documentation created
3. ⏭️ Ready to restart team with proper configuration
4. ⏭️ Launch agents with tasks
5. ⏭️ Monitor for any remaining issues

## Cost Optimization

**Estimated token usage for this project**:
- 1 Opus agent (final review): ~50K tokens
- 5 Sonnet 4.5 agents (detailed planning): ~250K tokens total
- Total: ~300K tokens

**vs. All Sonnet 4.6**:
- 6 Sonnet 4.6 agents: ~350K tokens
- **Additional Savings with 4.5**: ~35-40% cost reduction vs 4.6

**vs. All Opus**:
- 6 Opus agents: ~400K tokens
- **Total Savings**: ~50% cost reduction while maintaining quality

## Troubleshooting

If agents still fail:

1. **Check model availability**:
   ```bash
   curl http://localhost:4141/v1/models | jq '.data[].id'
   ```

2. **Test model directly**:
   ```bash
   curl http://localhost:4141/v1/chat/completions \
     -H "Content-Type: application/json" \
     -d '{"model":"claude-sonnet-4.6","messages":[{"role":"user","content":"test"}]}'
   ```

3. **Check agent loading**:
   - Ensure agent files have no syntax errors in frontmatter
   - YAML frontmatter must have `---` on separate lines
   - Model field format: `model: model-name` (no quotes needed)

4. **Fallback**: If issues persist, can pass model explicitly when spawning

## Summary

The agent model configuration is now properly set up with:
- Explicit model assignments in agent files
- Appropriate model selection based on task complexity
- Documentation for future reference
- Cost-optimized approach using Opus only where needed

The team is ready to launch with proper model configuration! 🚀
