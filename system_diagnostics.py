"""
system_diagnostics.py
LangChainAssistant/system_diagnostics.py
Version: 2.2.0 - System Diagnostics Module

COMMITTEE - System Diagnostics and Testing
==========================================

Comprehensive system diagnostics, testing, and information display functions
for COMMITTEE multi-agent code analysis system.

Authors: Baruch, Claude (Anthropic)
Project: LangChainAssistant / COMMITTEE
Created: July 2025

Functions:
- show_system_info(): Comprehensive system information
- run_system_tests(): System component testing
- show_pipeline_stats(): Multi-agent pipeline statistics
- show_version(): Version information
"""

import sys
import os
import time

# ===== CONSTANTS =====
VERSION = "2.2.0"
AUTHOR = "Baruch & Claude (Anthropic)"
PROJECT = "LangChainAssistant / COMMITTEE"


# ===== SETUP PATHS =====
def get_project_root():
    """Get project root directory"""
    return os.path.dirname(os.path.abspath(__file__))


PROJECT_ROOT = get_project_root()


# ===== IMPORT AVAILABILITY CHECK =====
def check_component_availability():
    """Check availability of core components"""
    # Try to import LLM utils
    try:
        from llm_utils import get_llm
        llm_available = True
    except ImportError:
        try:
            from src.llm_utils import get_llm
            llm_available = True
        except ImportError:
            llm_available = False

    # Try to import RAG utils
    try:
        from rag_utils import get_rag_chain
        rag_available = True
    except ImportError:
        try:
            from src.rag_utils import get_rag_chain
            rag_available = True
        except ImportError:
            rag_available = False

    # Try to import MultiAgent pipeline
    try:
        from multiagent_pipeline import MultiAgentPipeline
        multiagent_available = True
    except ImportError:
        try:
            from src.multiagent_pipeline import MultiAgentPipeline
            multiagent_available = True
        except ImportError:
            multiagent_available = False

    return llm_available, rag_available, multiagent_available


# ===== DIAGNOSTIC FUNCTIONS =====

def show_system_info():
    """Shows comprehensive system information"""
    print("🎯 COMMITTEE System Information")
    print("=" * 60)

    # Version info
    print(f"Version: {VERSION}")
    print(f"Authors: {AUTHOR}")
    print(f"Project: {PROJECT}")

    # Environment info
    print(f"\n🖥️ Environment:")
    print(f"   Project Root: {PROJECT_ROOT}")
    print(f"   Python Version: {sys.version}")
    print(f"   Platform: {sys.platform}")
    print(f"   Python Path Entries: {len(sys.path)}")

    # Component status
    llm_available, rag_available, multiagent_available = check_component_availability()

    print(f"\n🔧 Component Status:")
    print(f"   LLM Utils: {'✅ Available' if llm_available else '❌ Not available'}")
    print(f"   RAG Utils: {'✅ Available' if rag_available else '❌ Not available'}")
    print(f"   Multi-Agent: {'✅ Available' if multiagent_available else '❌ Not available'}")

    # Test LLM providers
    if llm_available:
        print(f"\n🤖 LLM Provider Status:")

        # Import LLM function
        try:
            from llm_utils import get_llm
        except ImportError:
            from src.llm_utils import get_llm

        providers = [
            ('gpt-4o', 'OpenAI GPT-4o'),
            ('gemini', 'Google Gemini'),
            ('claude', 'Anthropic Claude')
        ]

        for provider_id, provider_name in providers:
            try:
                llm = get_llm(provider_id)
                print(f"   {provider_name}: ✅ Available")
            except Exception as e:
                error_msg = str(e)
                if "api_key" in error_msg.lower():
                    print(f"   {provider_name}: ⚠️ API key required")
                elif "credentials" in error_msg.lower():
                    print(f"   {provider_name}: ⚠️ Credentials required")
                else:
                    print(f"   {provider_name}: ❌ Error: {error_msg[:40]}...")

    # Environment configuration
    print(f"\n🔑 Environment Configuration:")
    env_file = os.path.join(PROJECT_ROOT, '.env')
    if os.path.exists(env_file):
        print(f"   .env file: ✅ Found")

        # Check environment variables
        try:
            from dotenv import load_dotenv
            load_dotenv()

            env_vars = {
                'OPENAI_API_KEY': 'OpenAI API Key',
                'GOOGLE_API_KEY': 'Google API Key',
                'ANTHROPIC_API_KEY': 'Anthropic API Key',
                'LANGCHAIN_API_KEY': 'LangChain API Key'
            }

            for var_name, var_desc in env_vars.items():
                value = os.getenv(var_name)
                if value:
                    masked_value = value[:8] + "..." if len(value) > 8 else "***"
                    print(f"   {var_desc}: ✅ Set ({masked_value})")
                else:
                    print(f"   {var_desc}: ❌ Not set")

        except Exception as e:
            print(f"   .env processing: ❌ Error: {e}")
    else:
        print(f"   .env file: ❌ Not found")
        print(f"   Create .env file with your API keys:")
        print(f"     OPENAI_API_KEY=your_key_here")
        print(f"     GOOGLE_API_KEY=your_key_here")
        print(f"     ANTHROPIC_API_KEY=your_key_here")

    # Project structure
    print(f"\n📁 Project Structure:")
    folders_to_check = [
        ('src', 'Source code'),
        ('tests', 'Test files'),
        ('source_code', 'RAG source data'),
        ('data', 'Data files'),
        ('db', 'Vector database')
    ]

    for folder, description in folders_to_check:
        folder_path = os.path.join(PROJECT_ROOT, folder)
        if os.path.exists(folder_path):
            if os.path.isdir(folder_path):
                items = os.listdir(folder_path)
                files_count = len([f for f in items if os.path.isfile(os.path.join(folder_path, f))])
                dirs_count = len([f for f in items if os.path.isdir(os.path.join(folder_path, f))])
                print(f"   {folder}/: ✅ {description} ({files_count} files, {dirs_count} dirs)")
            else:
                print(f"   {folder}: ✅ {description} (file)")
        else:
            print(f"   {folder}/: ❌ Missing - {description}")

    # Overall health assessment
    print(f"\n📊 System Health Assessment:")
    total_components = 3
    working_components = sum([llm_available, rag_available, multiagent_available])
    health_percentage = (working_components / total_components) * 100

    if health_percentage == 100:
        status = "🟢 EXCELLENT - All components operational"
    elif health_percentage >= 66:
        status = "🟡 GOOD - Most components working"
    elif health_percentage >= 33:
        status = "🟠 PARTIAL - Limited functionality"
    else:
        status = "🔴 CRITICAL - System needs attention"

    print(f"   Overall Health: {health_percentage:.0f}% - {status}")
    print(
        f"   Ready for Production: {'✅ YES' if working_components >= 2 else '⚠️ PARTIAL' if working_components >= 1 else '❌ NO'}")

    # Recommendations
    print(f"\n💡 Recommendations:")
    if not llm_available:
        print(f"   - Install LLM dependencies: pip install openai google-generativeai anthropic")
    if not rag_available:
        print(f"   - Install RAG dependencies: pip install langchain chromadb")
    if not multiagent_available:
        print(f"   - Check multiagent_pipeline.py file exists in src/")
    if working_components == 0:
        print(f"   - Run: pip install -e . to reinstall the package")
        print(f"   - Check your .env file for API keys")


def show_pipeline_stats():
    """Shows multi-agent pipeline statistics"""
    llm_available, rag_available, multiagent_available = check_component_availability()

    if not multiagent_available:
        print("❌ Multi-agent pipeline not available")
        print("   Install dependencies and check multiagent_pipeline.py exists")
        return

    try:
        # Import MultiAgent pipeline
        try:
            from multiagent_pipeline import MultiAgentPipeline
        except ImportError:
            from src.multiagent_pipeline import MultiAgentPipeline

        pipeline = MultiAgentPipeline()
        stats = pipeline.get_pipeline_stats()

        print("📊 COMMITTEE Pipeline Statistics")
        print("=" * 40)

        if isinstance(stats, dict) and "error" not in stats:
            for key, value in stats.items():
                formatted_key = key.replace('_', ' ').title()
                print(f"   {formatted_key}: {value}")
        else:
            print(f"   {stats.get('error', 'No statistics available yet')}")
            print(f"   Run some code reviews to generate statistics")

    except Exception as e:
        print(f"❌ Failed to get pipeline stats: {e}")


def run_system_tests():
    """Runs comprehensive system tests"""
    print("🧪 Running COMMITTEE System Tests")
    print("=" * 50)

    tests_passed = 0
    total_tests = 0
    test_results = []

    llm_available, rag_available, multiagent_available = check_component_availability()

    # Test 1: Core imports
    total_tests += 1
    try:
        assert llm_available, "LLM utilities not available"
        test_results.append(("Core Imports", "PASSED", "✅"))
        tests_passed += 1
    except Exception as e:
        test_results.append(("Core Imports", f"FAILED: {e}", "❌"))

    # Test 2: LLM creation
    total_tests += 1
    if llm_available:
        try:
            # Import LLM function
            try:
                from llm_utils import get_llm
            except ImportError:
                from src.llm_utils import get_llm

            llm = get_llm('gpt-4o')
            test_results.append(("LLM Creation", "PASSED", "✅"))
            tests_passed += 1
        except Exception as e:
            test_results.append(("LLM Creation", f"FAILED: {e}", "❌"))
    else:
        test_results.append(("LLM Creation", "SKIPPED: LLM not available", "⏭️"))

    # Test 3: RAG system
    total_tests += 1
    if rag_available and llm_available:
        try:
            # Import functions
            try:
                from llm_utils import get_llm
                from rag_utils import get_rag_chain
            except ImportError:
                from src.llm_utils import get_llm
                from src.rag_utils import get_rag_chain

            llm = get_llm('gpt-4o')
            rag_chain = get_rag_chain(llm)
            test_results.append(("RAG System", "PASSED", "✅"))
            tests_passed += 1
        except Exception as e:
            test_results.append(("RAG System", f"FAILED: {e}", "❌"))
    else:
        test_results.append(("RAG System", "SKIPPED: RAG not available", "⏭️"))

    # Test 4: Multi-agent pipeline
    total_tests += 1
    if multiagent_available:
        try:
            # Import MultiAgent pipeline
            try:
                from multiagent_pipeline import MultiAgentPipeline
            except ImportError:
                from src.multiagent_pipeline import MultiAgentPipeline

            pipeline = MultiAgentPipeline()
            test_results.append(("Multi-Agent Pipeline", "PASSED", "✅"))
            tests_passed += 1
        except Exception as e:
            test_results.append(("Multi-Agent Pipeline", f"FAILED: {e}", "❌"))
    else:
        test_results.append(("Multi-Agent Pipeline", "SKIPPED: Not available", "⏭️"))

    # Test 5: Simple query functionality
    total_tests += 1
    if llm_available:
        try:
            # Import LLM function
            try:
                from llm_utils import get_llm
            except ImportError:
                from src.llm_utils import get_llm

            # Simple test query
            llm = get_llm('gpt-4o')
            # We can't actually test the query without API keys, but we can test creation
            test_results.append(("Simple Query Setup", "PASSED", "✅"))
            tests_passed += 1
        except Exception as e:
            test_results.append(("Simple Query Setup", f"FAILED: {e}", "❌"))
    else:
        test_results.append(("Simple Query Setup", "SKIPPED: LLM not available", "⏭️"))

    # Display results
    print("\n📋 Test Results:")
    for test_name, result, icon in test_results:
        print(f"   {icon} {test_name}: {result}")

    # Summary
    print(f"\n📊 Summary: {tests_passed}/{total_tests} tests passed")

    if tests_passed == total_tests:
        print("🎉 All tests passed! System is fully operational.")
    elif tests_passed >= total_tests * 0.7:
        print("⚠️ Most tests passed. System is functional with minor issues.")
    elif tests_passed > 0:
        print("🔧 Some tests failed. System needs configuration.")
    else:
        print("🚨 All tests failed. System requires attention.")

    return tests_passed, total_tests


def show_version():
    """Shows version information"""
    print(f"🎯 COMMITTEE AI Assistant")
    print(f"Version: {VERSION}")
    print(f"Authors: {AUTHOR}")
    print(f"Project: {PROJECT}")
    print(f"Python: {sys.version}")

    # Check component availability for version display
    llm_available, rag_available, multiagent_available = check_component_availability()
    print(f"Components: LLM={llm_available}, RAG={rag_available}, MultiAgent={multiagent_available}")


def check_api_keys():
    """Check API key availability and validity"""
    print("🔑 API Key Status Check")
    print("=" * 30)

    # Load environment variables
    try:
        from dotenv import load_dotenv
        load_dotenv()
    except ImportError:
        print("⚠️ python-dotenv not installed - using system environment only")

    # Check each API key
    api_keys = {
        'OPENAI_API_KEY': 'OpenAI',
        'GOOGLE_API_KEY': 'Google/Gemini',
        'ANTHROPIC_API_KEY': 'Anthropic/Claude',
        'LANGCHAIN_API_KEY': 'LangChain'
    }

    for key_name, service in api_keys.items():
        value = os.getenv(key_name)
        if value:
            if len(value) > 10:  # Reasonable minimum key length
                masked = value[:8] + "..." + value[-4:]
                print(f"   {service}: ✅ {masked}")
            else:
                print(f"   {service}: ⚠️ Key too short (may be invalid)")
        else:
            print(f"   {service}: ❌ Not set")

    return any(os.getenv(key) for key in api_keys.keys())


def quick_health_check():
    """Quick system health check"""
    print("⚡ Quick Health Check")
    print("=" * 20)

    llm_available, rag_available, multiagent_available = check_component_availability()

    components = [
        ("LLM Utils", llm_available),
        ("RAG Utils", rag_available),
        ("Multi-Agent", multiagent_available)
    ]

    working = sum(1 for _, available in components if available)
    total = len(components)

    print(f"Components: {working}/{total} working")
    for name, available in components:
        status = "✅" if available else "❌"
        print(f"   {status} {name}")

    # API Keys
    has_api_keys = check_api_keys()
    print(f"API Keys: {'✅ Configured' if has_api_keys else '❌ Missing'}")

    # Overall status
    if working == total and has_api_keys:
        print("🎉 System: READY")
    elif working > 0:
        print("⚠️ System: PARTIAL")
    else:
        print("🚨 System: NOT READY")


# ===== MAIN FUNCTIONS FOR STANDALONE EXECUTION =====
if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="COMMITTEE System Diagnostics")
    parser.add_argument('--info', action='store_true', help='Show system information')
    parser.add_argument('--test', action='store_true', help='Run system tests')
    parser.add_argument('--stats', action='store_true', help='Show pipeline stats')
    parser.add_argument('--version', action='store_true', help='Show version')
    parser.add_argument('--keys', action='store_true', help='Check API keys')
    parser.add_argument('--quick', action='store_true', help='Quick health check')

    args = parser.parse_args()

    if args.info:
        show_system_info()
    elif args.test:
        passed, total = run_system_tests()
        sys.exit(0 if passed == total else 1)
    elif args.stats:
        show_pipeline_stats()
    elif args.version:
        show_version()
    elif args.keys:
        check_api_keys()
    elif args.quick:
        quick_health_check()
    else:
        print("Use --help to see available options")
        quick_health_check()