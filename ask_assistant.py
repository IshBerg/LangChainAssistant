#!/usr/bin/env python3
"""
ask_assistant.py
LangChainAssistant/ask_assistant.py
Version: 2.2.0 - Modular CLI Interface

COMMITTEE - Enhanced AI Assistant (Main CLI)
============================================

Main command-line interface for COMMITTEE multi-agent code analysis system.
Core functionality with diagnostic functions moved to separate module.

Authors: Baruch, Claude (Anthropic)
Project: LangChainAssistant / COMMITTEE
Created: July 2025

Usage:
  python ask_assistant.py "query"
  python ask_assistant.py --review "task" --code "code"
  python ask_assistant.py --info
"""

import argparse
import sys
import os
from typing import Optional
import traceback
import time
from dotenv import load_dotenv
load_dotenv()
# ===== CONSTANTS =====
VERSION = "2.2.0"
AUTHOR = "Baruch & Claude (Anthropic)"
PROJECT = "LangChainAssistant / COMMITTEE"


# ===== PATH SETUP =====
def setup_paths():
    """Setup Python paths for imports"""
    current_dir = os.path.dirname(os.path.abspath(__file__))
    parent_dir = os.path.dirname(current_dir)
    src_dir = os.path.join(current_dir, 'src')

    paths_to_add = [current_dir, parent_dir, src_dir]

    for path in paths_to_add:
        if os.path.exists(path) and path not in sys.path:
            sys.path.insert(0, path)

    return current_dir


PROJECT_ROOT = setup_paths()


# ===== SAFE IMPORTS =====
def safe_import_llm():
    """Safe import of LLM utilities with multiple fallbacks"""
    import_attempts = [
        ('src.llm_utils', 'get_llm'),
        ('llm_utils', 'get_llm'),
        ('langchain_assistant_project.src.llm_utils', 'get_llm'),
    ]

    for module_name, func_name in import_attempts:
        try:
            module = __import__(module_name, fromlist=[func_name])
            return getattr(module, func_name)
        except ImportError:
            continue

    print(f"⚠️ Failed to import get_llm from any source")
    return None


def safe_import_rag():
    """Safe import of RAG utilities with multiple fallbacks"""
    import_attempts = [
        ('src.rag_utils', 'get_rag_chain'),
        ('rag_utils', 'get_rag_chain'),
        ('langchain_assistant_project.src.rag_utils', 'get_rag_chain'),
    ]

    for module_name, func_name in import_attempts:
        try:
            module = __import__(module_name, fromlist=[func_name])
            return getattr(module, func_name)
        except ImportError:
            continue

    print(f"⚠️ Failed to import get_rag_chain from any source")
    return None


def safe_import_multiagent():
    """Safe import of multi-agent pipeline with multiple fallbacks"""
    import_attempts = [
        ('src.multiagent_pipeline', 'MultiAgentPipeline'),
        ('multiagent_pipeline', 'MultiAgentPipeline'),
        ('langchain_assistant_project.src.multiagent_pipeline', 'MultiAgentPipeline'),
    ]

    for module_name, func_name in import_attempts:
        try:
            module = __import__(module_name, fromlist=[func_name])
            return getattr(module, func_name)
        except ImportError:
            continue

    print(f"⚠️ Multi-agent pipeline not available")
    return None


# ===== INITIALIZE IMPORTS =====
print("🔧 Initializing COMMITTEE components...")
get_llm = safe_import_llm()
get_rag_chain = safe_import_rag()
MultiAgentPipeline = safe_import_multiagent()

# Component availability flags
LLM_AVAILABLE = get_llm is not None
RAG_AVAILABLE = get_rag_chain is not None
MULTIAGENT_AVAILABLE = MultiAgentPipeline is not None

# Initial status
print(f"   LLM Utils: {'✅' if LLM_AVAILABLE else '❌'}")
print(f"   RAG Utils: {'✅' if RAG_AVAILABLE else '❌'}")
print(f"   Multi-Agent: {'✅' if MULTIAGENT_AVAILABLE else '❌'}")

# ===== IMPORT DIAGNOSTICS MODULE =====
try:
    from system_diagnostics import (
        show_system_info,
        show_pipeline_stats,
        run_system_tests,
        show_version
    )

    DIAGNOSTICS_AVAILABLE = True
except ImportError:
    print("⚠️ Diagnostics module not found - some features disabled")
    DIAGNOSTICS_AVAILABLE = False


# ===== TASK DETECTION =====
def detect_task_type(query: str, code: str = "") -> str:
    """Automatically detects the type of task based on query and code presence"""
    query_lower = query.lower()

    # Code review keywords
    review_keywords = [
        'ревью', 'review', 'улучшения', 'improvements', 'рефакторинг', 'refactor',
        'оптимизация', 'optimization', 'анализ кода', 'code analysis',
        'проверь код', 'check code', 'предложи улучшения', 'suggest improvements',
        'исправь', 'fix', 'багы', 'bugs'
    ]

    # Multi-agent keywords (complex analysis)
    multiagent_keywords = [
        'архитектура', 'architecture', 'паттерны', 'patterns', 'дизайн', 'design',
        'безопасность', 'security', 'производительность', 'performance',
        'комплексный анализ', 'comprehensive analysis', 'детальный анализ', 'detailed analysis',
        'принципы', 'principles', 'solid'
    ]

    # If code is provided and review keywords are present
    if code and any(keyword in query_lower for keyword in review_keywords):
        return 'code_review' if MULTIAGENT_AVAILABLE else 'simple'

    # If complex analysis keywords are present
    if any(keyword in query_lower for keyword in multiagent_keywords):
        return 'multiagent' if MULTIAGENT_AVAILABLE else 'simple'

    # Default to simple query
    return 'simple'


# ===== CORE FUNCTIONS =====
def simple_query(query: str, use_rag: bool = True, verbose: bool = False) -> str:
    """Handles simple queries with proper error handling"""
    if not LLM_AVAILABLE:
        return "❌ LLM utilities not available. Please check your installation and API keys."

    try:
        if verbose:
            print("🤖 Processing simple query...")

        if use_rag and RAG_AVAILABLE:
            if verbose:
                print("📚 Using RAG context...")
            try:
                llm = get_llm('gpt-4o')
                rag_chain = get_rag_chain(llm)
                response = rag_chain.invoke(query)
                return response
            except Exception as rag_error:
                if verbose:
                    print(f"⚠️ RAG failed: {rag_error}")
                    print("🔄 Falling back to direct LLM...")
                llm = get_llm('gpt-4o')
                response = llm.invoke(query)
                return response
        else:
            if verbose:
                print("💭 Direct LLM query...")
            llm = get_llm('gpt-4o')
            response = llm.invoke(query)
            return response

    except Exception as e:
        error_msg = f"❌ Query failed: {str(e)}"
        if verbose:
            print(error_msg)
            print("Full traceback:")
            traceback.print_exc()
        return error_msg


def code_review_task(query: str, code: str, verbose: bool = False) -> str:
    """Handles code review with graceful degradation"""
    if not MULTIAGENT_AVAILABLE:
        if verbose:
            print("⚠️ Multi-agent pipeline not available, using enhanced RAG")

        enhanced_query = f"""
Проведи детальный анализ следующего кода как опытный архитектор:

КОД ДЛЯ АНАЛИЗА:
```
{code}
```

ЗАДАЧА: {query}

Проанализируй по следующим критериям:

🔍 СТРУКТУРНЫЙ АНАЛИЗ:
- Архитектурные паттерны
- Организация кода
- Принципы SOLID

💪 СИЛЬНЫЕ СТОРОНЫ:
- Что сделано хорошо
- Соответствие best practices
- Удачные решения

⚠️ ПРОБЛЕМЫ И РИСКИ:
- Потенциальные баги
- Проблемы производительности
- Нарушения принципов

🔧 РЕКОМЕНДАЦИИ:
- Конкретные улучшения
- Альтернативные подходы
- Приоритет изменений

📚 РЕСУРСЫ:
- Паттерны для изучения
- Дополнительные материалы
"""
        return simple_query(enhanced_query, use_rag=True, verbose=verbose)

    try:
        if verbose:
            print("👥 Starting multi-agent code review...")

        pipeline = MultiAgentPipeline()
        results = pipeline.execute_code_review(code, query, use_rag=RAG_AVAILABLE)
        return pipeline.format_results(results)

    except Exception as e:
        if verbose:
            print(f"❌ Multi-agent failed: {str(e)}")
            print("🔄 Falling back to enhanced RAG...")

        enhanced_query = f"Детальный анализ кода: {query}\n\nКод для анализа:\n```\n{code}\n```"
        return simple_query(enhanced_query, use_rag=True, verbose=verbose)


def multiagent_analysis(query: str, code: str = "", verbose: bool = False) -> str:
    """Handles complex analysis using full multi-agent pipeline"""
    if not MULTIAGENT_AVAILABLE:
        if verbose:
            print("⚠️ Multi-agent pipeline not available, using enhanced RAG")

        enhanced_query = f"Проведи комплексный архитектурный анализ:\n\nЗапрос: {query}"
        if code:
            enhanced_query += f"\n\nКод для анализа:\n```\n{code}\n```"
        return simple_query(enhanced_query, use_rag=True, verbose=verbose)

    try:
        if verbose:
            print("🏛️ Starting comprehensive multi-agent analysis...")

        if code:
            # Code-based analysis
            pipeline = MultiAgentPipeline()
            results = pipeline.execute_code_review(code, query, use_rag=RAG_AVAILABLE)
            return pipeline.format_results(results)
        else:
            # Query-based analysis (future enhancement)
            if verbose:
                print("⚠️ Multi-agent for non-code queries not yet implemented")
                print("🔄 Falling back to enhanced RAG query...")
            return simple_query(query, use_rag=True, verbose=verbose)

    except Exception as e:
        if verbose:
            print(f"❌ Multi-agent analysis failed: {str(e)}")
            print("🔄 Falling back to simple query...")

        enhanced_query = f"Комплексный анализ:\n\nЗапрос: {query}"
        if code:
            enhanced_query += f"\n\nКод:\n```\n{code}\n```"
        return simple_query(enhanced_query, use_rag=True, verbose=verbose)


def read_code_from_stdin() -> Optional[str]:
    """Reads code from stdin if available (for IDE integration)"""
    try:
        if not sys.stdin.isatty():  # Data is being piped
            code = sys.stdin.read().strip()
            if code:
                print(f"📄 Code received from stdin ({len(code)} characters)")
                return code
    except Exception as e:
        print(f"⚠️ Failed to read from stdin: {e}")

    return None


# ===== MAIN FUNCTION =====
def main():
    """Main function with comprehensive argument handling"""

    parser = argparse.ArgumentParser(
        description=f"COMMITTEE AI Assistant v{VERSION}",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=f"""
Examples:
  Simple query:
    python ask_assistant.py "Что такое dependency injection в Android?"

  Code review:
    python ask_assistant.py --review "Проведи ревью" --code "class Example{{...}}"

  Multi-agent analysis:
    python ask_assistant.py --multiagent "Анализ архитектуры" --code "..."

  Auto-detection:
    python ask_assistant.py "Предложи улучшения для кода: class Test{{...}}"

  System diagnostics:
    python ask_assistant.py --info
    python ask_assistant.py --test
    python ask_assistant.py --stats

  Pipe code from IDE:
    echo "class Test{{}}" | python ask_assistant.py "анализ кода"

Version: {VERSION}
Authors: {AUTHOR}
        """
    )

    # ===== MAIN ARGUMENTS =====
    parser.add_argument('query', nargs='?', help='Question or task description')
    parser.add_argument('--code', help='Code to analyze')

    # ===== OPERATION MODES =====
    parser.add_argument('--review', action='store_true', help='Force code review mode')
    parser.add_argument('--multiagent', action='store_true', help='Force multi-agent analysis')
    parser.add_argument('--simple', action='store_true', help='Force simple query mode')

    # ===== CONFIGURATION =====
    parser.add_argument('--no-rag', action='store_true', help='Disable RAG context')
    parser.add_argument('--verbose', '-v', action='store_true', help='Verbose output')
    parser.add_argument('--debug', action='store_true', help='Debug mode with full tracebacks')

    # ===== DIAGNOSTIC COMMANDS =====
    parser.add_argument('--info', action='store_true', help='Show detailed system information')
    parser.add_argument('--stats', action='store_true', help='Show pipeline statistics')
    parser.add_argument('--test', action='store_true', help='Run comprehensive system tests')
    parser.add_argument('--version', action='store_true', help='Show version information')

    args = parser.parse_args()

    # ===== HANDLE DIAGNOSTIC COMMANDS =====

    if args.version:
        if DIAGNOSTICS_AVAILABLE:
            show_version()
        else:
            print(f"🎯 COMMITTEE AI Assistant v{VERSION}")
            print(f"Authors: {AUTHOR}")
        return

    if args.info:
        if DIAGNOSTICS_AVAILABLE:
            show_system_info()
        else:
            print("❌ System diagnostics not available - system_diagnostics.py missing")
        return

    if args.stats:
        if DIAGNOSTICS_AVAILABLE:
            show_pipeline_stats()
        else:
            print("❌ Pipeline statistics not available - system_diagnostics.py missing")
        return

    if args.test:
        if DIAGNOSTICS_AVAILABLE:
            passed, total = run_system_tests()
            sys.exit(0 if passed == total else 1)
        else:
            print("❌ System tests not available - system_diagnostics.py missing")
            sys.exit(1)

    # ===== VALIDATE MAIN OPERATION =====

    if not args.query:
        print("❌ Query required. Use --help for examples.")
        if not any([LLM_AVAILABLE, RAG_AVAILABLE]):
            print("\n⚠️ No components available. Run:")
            print("   python ask_assistant.py --info")
            print("   python ask_assistant.py --test")
        sys.exit(1)

    # ===== PREPARE EXECUTION =====

    query = args.query
    code = args.code or read_code_from_stdin() or ""

    # Determine task type
    if args.simple:
        task_type = 'simple'
    elif args.review:
        task_type = 'code_review'
    elif args.multiagent:
        task_type = 'multiagent'
    else:
        task_type = detect_task_type(query, code)

    if args.verbose:
        print(f"🎯 Execution Details:")
        print(f"   Task Type: {task_type}")
        print(f"   Query Length: {len(query)} characters")
        print(f"   Code Provided: {'Yes' if code else 'No'} ({len(code)} chars)")
        print(f"   RAG Enabled: {not args.no_rag}")
        print(f"   Debug Mode: {args.debug}")
        print("=" * 50)

    # ===== EXECUTE TASK =====

    start_time = time.time()

    try:
        if task_type == 'simple':
            result = simple_query(query, use_rag=not args.no_rag, verbose=args.verbose)
        elif task_type == 'code_review':
            if not code:
                print("❌ Code review requires code. Use --code argument or pipe code via stdin.")
                print("   Example: echo 'class Test{}' | python ask_assistant.py 'review this'")
                sys.exit(1)
            result = code_review_task(query, code, verbose=args.verbose)
        elif task_type == 'multiagent':
            result = multiagent_analysis(query, code, verbose=args.verbose)
        else:
            result = "❌ Unknown task type detected"

        execution_time = time.time() - start_time

        # ===== OUTPUT RESULTS =====

        print(f"\n🎯 COMMITTEE RESULT:")
        print("=" * 60)
        print(result)

        if args.verbose:
            print(f"\n⏱️ Execution completed in {execution_time:.2f} seconds")
            print(
                f"📊 Components used: LLM={LLM_AVAILABLE}, RAG={not args.no_rag and RAG_AVAILABLE}, MultiAgent={task_type in ['code_review', 'multiagent'] and MULTIAGENT_AVAILABLE}")

    except KeyboardInterrupt:
        print("\n⚠️ Operation interrupted by user")
        sys.exit(1)
    except Exception as e:
        execution_time = time.time() - start_time
        print(f"\n❌ Execution failed after {execution_time:.2f}s: {str(e)}")

        if args.debug or args.verbose:
            print("\n🔍 Debug Information:")
            traceback.print_exc()
            print(f"\nSystem Status:")
            print(f"   LLM Available: {LLM_AVAILABLE}")
            print(f"   RAG Available: {RAG_AVAILABLE}")
            print(f"   MultiAgent Available: {MULTIAGENT_AVAILABLE}")

        sys.exit(1)


# ===== ENTRY POINT =====
if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"\n💥 Critical error: {e}")
        print("\n🔧 Troubleshooting:")
        print("   1. Check your Python environment")
        print("   2. Run: pip install -e .")
        print("   3. Check: python ask_assistant.py --info")
        print("   4. Verify: python ask_assistant.py --test")
        sys.exit(1)