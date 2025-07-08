"""
multiagent_pipeline.py
LangChainAssistant/multiagent_pipeline.py
Version: 1.0.0

COMMITTEE - Multi-Agent Pipeline: Coder → Advisor
================================================

First implementation of multi-agent code review pipeline.
Sequential execution: Gemini analyzes → GPT-4o advises improvements.

Authors: Baruch, Claude (Anthropic)
Project: LangChainAssistant / COMMITTEE
Created: July 2025
"""

import os
import json
from typing import Dict, Any, Tuple
from dataclasses import dataclass
from enum import Enum

# Import our existing utilities
from llm_utils import get_llm
from rag_utils import get_rag_chain


class AgentRole(Enum):
    CODER = "coder"
    ADVISOR = "advisor"
    AUDITOR = "auditor"


@dataclass
class AgentResult:
    """Standard result structure from any agent"""
    agent_name: str
    role: AgentRole
    analysis: str
    recommendations: list
    confidence: float
    execution_time: float
    raw_response: str


class CoderAgent:
    """
    Gemini-powered agent for code analysis
    Role: Analyze code structure, logic, and identify strengths
    """

    def __init__(self):
        self.llm = get_llm('gemini')
        self.name = "Gemini-Coder"

    def analyze_code(self, code: str, context: str = "") -> AgentResult:
        """
        Analyzes code structure and logic
        """
        import time
        start_time = time.time()

        prompt = self._build_analysis_prompt(code, context)

        try:
            response = self.llm.invoke(prompt)

            # Parse structured response
            analysis, recommendations, confidence = self._parse_response(response)

            execution_time = time.time() - start_time

            return AgentResult(
                agent_name=self.name,
                role=AgentRole.CODER,
                analysis=analysis,
                recommendations=recommendations,
                confidence=confidence,
                execution_time=execution_time,
                raw_response=response
            )

        except Exception as e:
            execution_time = time.time() - start_time
            return AgentResult(
                agent_name=self.name,
                role=AgentRole.CODER,
                analysis=f"❌ Analysis failed: {str(e)}",
                recommendations=[],
                confidence=0.0,
                execution_time=execution_time,
                raw_response=""
            )

    def _build_analysis_prompt(self, code: str, context: str) -> str:
        """Builds analysis prompt for Gemini"""

        prompt = f"""
Ты — эксперт-аналитик кода в команде разработчиков. 
Твоя задача: провести детальный анализ предоставленного кода.

КОНТЕКСТ ПРОЕКТА:
{context if context else "Android приложение YAKKI на Kotlin"}

КОД ДЛЯ АНАЛИЗА:
```
{code}
```

ВЫПОЛНИ АНАЛИЗ ПО СЛЕДУЮЩЕЙ СТРУКТУРЕ:

## 🔍 СТРУКТУРНЫЙ АНАЛИЗ
- Архитектурный паттерн (если применим)
- Основные компоненты и их назначение
- Зависимости между компонентами

## 💪 СИЛЬНЫЕ СТОРОНЫ
- Что сделано хорошо
- Соответствие best practices
- Удачные архитектурные решения

## ⚡ ЛОГИКА РАБОТЫ
- Пошаговое описание алгоритма
- Ключевые методы и их роль
- Потоки данных

## 📊 ОЦЕНКА КАЧЕСТВА
- Читаемость кода (1-10)
- Соответствие стандартам (1-10)
- Общая уверенность в анализе (1-10)

ОТВЕЧАЙ СТРУКТУРИРОВАННО НА РУССКОМ ЯЗЫКЕ.
В конце обязательно укажи: CONFIDENCE_SCORE: [число от 1 до 10]
"""
        return prompt

    def _parse_response(self, response: str) -> Tuple[str, list, float]:
        """Parses Gemini response"""
        try:
            # Extract confidence score
            confidence = 0.7  # default
            if "CONFIDENCE_SCORE:" in response:
                import re
                match = re.search(r'CONFIDENCE_SCORE:\s*(\d+)', response)
                if match:
                    confidence = float(match.group(1)) / 10.0

            # Extract recommendations (basic parsing)
            recommendations = []
            lines = response.split('\n')
            for line in lines:
                if '- ' in line and any(
                        word in line.lower() for word in ['рекомендую', 'следует', 'можно', 'улучшить']):
                    recommendations.append(line.strip())

            return response, recommendations, confidence

        except Exception as e:
            return response, [], 0.5


class AdvisorAgent:
    """
    GPT-4o powered agent for improvement recommendations
    Role: Analyze coder's output and propose specific improvements
    """

    def __init__(self):
        self.llm = get_llm('gpt-4o')
        self.name = "GPT4o-Advisor"

    def provide_advice(self, code: str, coder_analysis: AgentResult, context: str = "") -> AgentResult:
        """
        Provides improvement recommendations based on coder analysis
        """
        import time
        start_time = time.time()

        prompt = self._build_advice_prompt(code, coder_analysis, context)

        try:
            response = self.llm.invoke(prompt)

            # Parse structured response
            analysis, recommendations, confidence = self._parse_response(response)

            execution_time = time.time() - start_time

            return AgentResult(
                agent_name=self.name,
                role=AgentRole.ADVISOR,
                analysis=analysis,
                recommendations=recommendations,
                confidence=confidence,
                execution_time=execution_time,
                raw_response=response
            )

        except Exception as e:
            execution_time = time.time() - start_time
            return AgentResult(
                agent_name=self.name,
                role=AgentRole.ADVISOR,
                analysis=f"❌ Advice generation failed: {str(e)}",
                recommendations=[],
                confidence=0.0,
                execution_time=execution_time,
                raw_response=""
            )

    def _build_advice_prompt(self, code: str, coder_analysis: AgentResult, context: str) -> str:
        """Builds advice prompt for GPT-4o"""

        prompt = f"""
You are a Senior Software Architect reviewing code analysis from a junior developer.
Your role: Provide specific, actionable improvement recommendations.

PROJECT CONTEXT:
{context if context else "Android YAKKI application in Kotlin"}

ORIGINAL CODE:
```
{code}
```

ANALYSIS FROM CODER ({coder_analysis.agent_name}):
{coder_analysis.analysis}

CODER'S CONFIDENCE: {coder_analysis.confidence:.1f}/1.0

YOUR TASK: 
Based on the coder's analysis and your expertise, provide SPECIFIC, ACTIONABLE recommendations.

STRUCTURE YOUR RESPONSE AS:

## 🎯 IMPROVEMENT PRIORITIES
1. Critical issues (security, performance, bugs)
2. Architecture improvements  
3. Code quality enhancements

## 🔧 SPECIFIC RECOMMENDATIONS
For each recommendation, provide:
- What to change
- Why this change is beneficial  
- Code example (if applicable)
- Expected impact

## 🚀 REFACTORING OPPORTUNITIES
- Design pattern improvements
- Performance optimizations
- Maintainability enhancements

## 📋 IMPLEMENTATION ROADMAP
Priority order for implementing changes

## 🎖️ CONFIDENCE ASSESSMENT
Rate your confidence in these recommendations (1-10)

PROVIDE CONCRETE, IMPLEMENTABLE ADVICE.
End with: CONFIDENCE_SCORE: [number 1-10]
"""
        return prompt

    def _parse_response(self, response: str) -> Tuple[str, list, float]:
        """Parses GPT-4o response"""
        try:
            # Extract confidence score
            confidence = 0.8  # default for GPT-4o
            if "CONFIDENCE_SCORE:" in response:
                import re
                match = re.search(r'CONFIDENCE_SCORE:\s*(\d+)', response)
                if match:
                    confidence = float(match.group(1)) / 10.0

            # Extract recommendations
            recommendations = []
            lines = response.split('\n')
            current_rec = ""

            for line in lines:
                # Look for numbered recommendations
                if re.match(r'^\d+\.', line.strip()):
                    if current_rec:
                        recommendations.append(current_rec.strip())
                    current_rec = line.strip()
                elif line.startswith('- ') and current_rec:
                    current_rec += " " + line.strip()
                elif current_rec and line.strip() and not line.startswith('#'):
                    current_rec += " " + line.strip()
                elif line.startswith('#') and current_rec:
                    recommendations.append(current_rec.strip())
                    current_rec = ""

            if current_rec:
                recommendations.append(current_rec.strip())

            return response, recommendations, confidence

        except Exception as e:
            return response, [], 0.6


class MultiAgentPipeline:
    """
    Orchestrates the Coder → Advisor pipeline
    """

    def __init__(self):
        self.coder = CoderAgent()
        self.advisor = AdvisorAgent()
        self.results_history = []

    def execute_code_review(self, code: str, context: str = "", use_rag: bool = True) -> Dict[str, Any]:
        """
        Executes complete code review pipeline
        """
        import time
        pipeline_start = time.time()

        print("🚀 Starting Multi-Agent Code Review Pipeline...")
        print("=" * 60)

        # Step 0: Enhance context with RAG if enabled
        enhanced_context = context
        if use_rag and context:
            try:
                print("📚 Enhancing context with RAG...")
                # Simple RAG context enhancement
                rag_llm = get_llm('gpt-4o')
                rag_chain = get_rag_chain(rag_llm)
                rag_context = rag_chain.invoke(f"Context for code review: {context}")
                enhanced_context = f"{context}\n\nRAG Context:\n{rag_context}"
                print("✅ Context enhanced with project knowledge")
            except Exception as e:
                print(f"⚠️ RAG enhancement failed: {e}")

        # Step 1: Coder Analysis
        print("\n🔍 Step 1: Coder Analysis (Gemini)")
        print("-" * 30)
        coder_result = self.coder.analyze_code(code, enhanced_context)
        print(f"✅ Coder completed in {coder_result.execution_time:.2f}s")
        print(f"📊 Confidence: {coder_result.confidence:.1f}/1.0")

        # Step 2: Advisor Recommendations
        print("\n💡 Step 2: Advisor Recommendations (GPT-4o)")
        print("-" * 30)
        advisor_result = self.advisor.provide_advice(code, coder_result, enhanced_context)
        print(f"✅ Advisor completed in {advisor_result.execution_time:.2f}s")
        print(f"📊 Confidence: {advisor_result.confidence:.1f}/1.0")

        # Compile results
        pipeline_time = time.time() - pipeline_start

        results = {
            "pipeline_version": "1.0.0",
            "total_execution_time": pipeline_time,
            "original_code": code,
            "context": enhanced_context,
            "coder_analysis": coder_result,
            "advisor_recommendations": advisor_result,
            "success": True,
            "timestamp": time.time()
        }

        # Store in history
        self.results_history.append(results)

        print(f"\n🎉 Pipeline completed in {pipeline_time:.2f}s")
        print("=" * 60)

        return results

    def format_results(self, results: Dict[str, Any]) -> str:
        """
        Formats pipeline results for display
        """
        coder = results["coder_analysis"]
        advisor = results["advisor_recommendations"]

        report = f"""
🎯 COMMITTEE CODE REVIEW REPORT
{'=' * 50}

⏱️ Execution Time: {results['total_execution_time']:.2f}s
📅 Timestamp: {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(results['timestamp']))}

🔍 CODER ANALYSIS ({coder.agent_name})
{'-' * 30}
Confidence: {coder.confidence:.1f}/1.0 | Time: {coder.execution_time:.2f}s

{coder.analysis}

💡 ADVISOR RECOMMENDATIONS ({advisor.agent_name})  
{'-' * 30}
Confidence: {advisor.confidence:.1f}/1.0 | Time: {advisor.execution_time:.2f}s

{advisor.analysis}

📋 SUMMARY
{'-' * 30}
- Coder found {len(coder.recommendations)} key points
- Advisor provided {len(advisor.recommendations)} recommendations
- Overall pipeline confidence: {(coder.confidence + advisor.confidence) / 2:.1f}/1.0

🚀 NEXT STEPS
{'-' * 30}
Review advisor recommendations and implement in priority order.
"""

        return report

    def get_pipeline_stats(self) -> Dict[str, Any]:
        """
        Returns pipeline performance statistics
        """
        if not self.results_history:
            return {"error": "No pipeline executions recorded"}

        total_runs = len(self.results_history)
        avg_time = sum(r["total_execution_time"] for r in self.results_history) / total_runs
        avg_coder_confidence = sum(r["coder_analysis"].confidence for r in self.results_history) / total_runs
        avg_advisor_confidence = sum(r["advisor_recommendations"].confidence for r in self.results_history) / total_runs

        return {
            "total_pipeline_runs": total_runs,
            "average_execution_time": avg_time,
            "average_coder_confidence": avg_coder_confidence,
            "average_advisor_confidence": avg_advisor_confidence,
            "last_run_time": self.results_history[-1]["timestamp"]
        }


# Integration function for ask_assistant.py
def run_multiagent_review(code: str, task_description: str = "") -> str:
    """
    Drop-in replacement for simple LLM calls in ask_assistant.py
    """
    try:
        pipeline = MultiAgentPipeline()
        results = pipeline.execute_code_review(code, task_description, use_rag=True)
        return pipeline.format_results(results)
    except Exception as e:
        return f"❌ Multi-agent pipeline failed: {str(e)}"


# Example usage
if __name__ == "__main__":
    # Test with sample Kotlin code
    test_code = '''
class UserRepository {
    private val api = ApiService()

    fun getUser(id: String): User? {
        try {
            val response = api.fetchUser(id)
            if (response.isSuccessful) {
                return response.body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
'''

    pipeline = MultiAgentPipeline()
    results = pipeline.execute_code_review(
        code=test_code,
        context="Android YAKKI app - user data management"
    )

    print(pipeline.format_results(results))
    print("\n" + "=" * 60)
    print("📊 Pipeline Statistics:")
    stats = pipeline.get_pipeline_stats()
    for key, value in stats.items():
        print(f"{key}: {value}")