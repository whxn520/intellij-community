package com.intellij.stats.personalization.impl

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.completion.FeatureManagerImpl
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.stats.personalization.UserFactor
import com.intellij.stats.personalization.UserFactorsManager
import com.jetbrains.completion.ranker.features.BinaryFeature
import com.jetbrains.completion.ranker.features.CatergorialFeature
import com.jetbrains.completion.ranker.features.DoubleFeature
import com.jetbrains.completion.ranker.features.impl.FeatureUtils

/**
 * @author Vitaliy.Bibaev
 */
class UserFactorsManagerImpl : UserFactorsManager, ProjectComponent {
    private companion object {
        val LOG = Logger.getInstance(UserFactorsManagerImpl::class.java)
    }
    private val userFactors = mutableMapOf<String, UserFactor>()
    init {
        // user factors
        register(ExplicitCompletionRatio())
        register(CompletionTypeRatio(CompletionType.BASIC))
        register(CompletionTypeRatio(CompletionType.SMART))
        register(CompletionTypeRatio(CompletionType.CLASS_NAME))

        register(TodayCompletionUsageCount())
        register(TotalUsageCount())
        register(WeekAverageUsageCount())

        register(MostFrequentPrefixLength())
        register(AveragePrefixLength())

        register(AverageSelectedItemPosition())
        register(MaxSelectedItemPosition())
        register(MostFrequentSelectedItemPosition())

        register(AverageTimeBetweenTyping())

        register(MnemonicsRatio())

        // feature-derived factors
        val featureManager = FeatureManagerImpl.getInstance()
        featureManager.binaryFactors.forEach(this::registerBinaryFeatureDerivedFactors)
        featureManager.doubleFactors.forEach(this::registerDoubleFeatureDerivedFactors)
        featureManager.categorialFactors.forEach(this::registerCategorialFeatureDerivedFactors)
    }

    private fun registerBinaryFeatureDerivedFactors(feature: BinaryFeature) {
        register(BinaryValueRatio(feature, feature.availableValues.first))
        register(BinaryValueRatio(feature, feature.availableValues.second))
    }

    private fun registerDoubleFeatureDerivedFactors(feature: DoubleFeature) {
        register(MaxDoubleFeatureValue(feature))
        register(MinDoubleFeatureValue(feature))
        register(AverageDoubleFeatureValue(feature))
        register(UndefinedDoubleFeatureValueRatio(feature))
    }

    private fun registerCategorialFeatureDerivedFactors(feature: CatergorialFeature) {
        feature.categories.forEach { register(CategoryRatio(feature, it)) }
        register(CategoryRatio(feature, FeatureUtils.OTHER))
        register(MostFrequentCategory(feature))
    }

    override fun getAllFactors(): List<UserFactor> = userFactors.values.toList()

    override fun getAllFactorIds(): List<String> = userFactors.keys.toList()

    override fun getFactor(id: String): UserFactor = userFactors[id]!!

    private fun register(factor: UserFactor) {
        val old = userFactors.put(factor.id, factor)
        if (old != null) {
            if (old === factor) {
                LOG.warn("The same factor was registered twice")
            } else {
                LOG.warn("Two different factors with the same id found: id = ${old.id}, " +
                        "classes = ${listOf(factor.javaClass.canonicalName, old.javaClass.canonicalName)}")
            }
        }
    }
}