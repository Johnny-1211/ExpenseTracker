package com.example.expensetracker.report

import android.R
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.R.color
import com.example.expensetracker.adapter.ReportTansactionAdapter
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ActivityReportBinding
import com.example.expensetracker.repository.ExpenseRepository
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.coroutines.launch


class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding
    private val TAG = this.javaClass.canonicalName
    private lateinit var expenseRepository: ExpenseRepository

    private lateinit var incomeTransactionList : MutableList<Expense>
    private lateinit var expenseTransactionList : MutableList<Expense>
    private lateinit var reportAdapter : ReportTansactionAdapter

    // List of pie chart entry
    private var pieChartIncomeDataList : MutableList<PieEntry> = mutableListOf()
    private var pieChartExpenseDataList : MutableList<PieEntry> = mutableListOf()

    // List of bar chart entry
    private var barChartIncomeDataList : MutableList<BarEntry> = mutableListOf()
    private var barChartExpenseDataList : MutableList<BarEntry> = mutableListOf()

    // chart variable
    private lateinit var pieChart : PieChart

    // List of Income amount from different category
    private var houseIncomeAmountList : MutableList<Double> = mutableListOf()
    private var foodIncomeAmountList : MutableList<Double> = mutableListOf()
    private var socialIncomeAmountList : MutableList<Double> = mutableListOf()
    private var clothesIncomeAmountList : MutableList<Double> = mutableListOf()
    private var transIncomeAmountList : MutableList<Double> = mutableListOf()
    private var otherIncomeAmountList : MutableList<Double> = mutableListOf()

    // List of Expense amount from different category
    private var houseExpenseAmountList : MutableList<Double> = mutableListOf()
    private var foodExpenseAmountList : MutableList<Double> = mutableListOf()
    private var socialExpenseAmountList : MutableList<Double> = mutableListOf()
    private var clothesExpenseAmountList : MutableList<Double> = mutableListOf()
    private var transExpenseAmountList : MutableList<Double> = mutableListOf()
    private var otherExpenseAmountList : MutableList<Double> = mutableListOf()

    // Sum of the list of amount either Income or expense
    private var houseTotal = 0f
    private var foodTotal = 0f
    private var socialTotal = 0f
    private var clothesTotal = 0f
    private var transTotal = 0f
    private var otherTotal = 0f

    private var totalIncome = 0.0
    private var totalExpense = 0.0

    private var tfRegular: Typeface? = null
    private var tfLight: Typeface? = null

    private lateinit var switchOnOff: RadioGroup
    private var currentClickedType = "Income"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pieChart = binding.pieChart
        expenseRepository = ExpenseRepository(application)

        setupToolbar()

        incomeTransactionList = mutableListOf()
        expenseTransactionList = mutableListOf()

        reportAdapter = ReportTansactionAdapter(applicationContext, incomeTransactionList)
        setupRecyclerView()

        switchOnOff = binding.toggle
        setupToggleListener()

        observeTotalAmounts()
        observeTransactionLists()
    }

    private fun chartDataPrepare(currentClickedType: String) {
        val incomeAmountLists = mapOf(
            "House" to houseIncomeAmountList,
            "Food" to foodIncomeAmountList,
            "Social" to socialIncomeAmountList,
            "Clothes" to clothesIncomeAmountList,
            "Transportation" to transIncomeAmountList,
            "Others" to otherIncomeAmountList
        )

        val expenseAmountLists = mapOf(
            "House" to houseExpenseAmountList,
            "Food" to foodExpenseAmountList,
            "Social" to socialExpenseAmountList,
            "Clothes" to clothesExpenseAmountList,
            "Transportation" to transExpenseAmountList,
            "Others" to otherExpenseAmountList
        )

        val chartDataList = if (currentClickedType == "Income") pieChartIncomeDataList else pieChartExpenseDataList
        val barChartDataList = if (currentClickedType == "Income") barChartIncomeDataList else barChartExpenseDataList

        chartDataList.clear()
        barChartDataList.clear()

        val amountList = if (currentClickedType == "Income") incomeAmountLists else expenseAmountLists

        amountList.forEach { (category, amountList) ->
            if (amountList.isNotEmpty()) {
                val total = amountList.sum().toFloat()
                chartDataList.add(PieEntry(total, category))
                barChartDataList.add(BarEntry(barChartDataList.size.toFloat(), total))
            }
        }

        Log.d("pieChartExpenseDataList", "$chartDataList")
    }

    private fun displayPieChart(currentClickedType: String) {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5F, 10F, 5F, 5F)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.setCenterTextTypeface(tfLight)
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.rotationAngle = 0.toFloat()
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.spin(2000, 0F, 360F, Easing.EaseInOutQuad)

        val l = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTypeface(tfRegular)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setCenterTextSize(20f)
        pieChart.setEntryLabelTextSize(15f)

        // Customize the appearance of the chart
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(android.R.color.transparent)
        pieChart.setTransparentCircleColor(android.R.color.transparent)
        pieChart.highlightValues(null)

        val dataSet = PieDataSet(
            if (currentClickedType == "Income") pieChartIncomeDataList else pieChartExpenseDataList,
            "Category"
        )

        dataSet.valueTextSize = 15f
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0F, 40F)
        dataSet.selectionShift = 5f
        dataSet.colors = getColors()

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        data.setValueTypeface(tfLight)
        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.data = data
        pieChart.setUsePercentValues(true)

        pieChart.centerText = "$${if (currentClickedType == "Income") totalIncome else totalExpense}"

        pieChart.invalidate()
    }

    @SuppressLint("ResourceType")
    private fun getColors(): ArrayList<Int> {
        val colors: ArrayList<Int> = ArrayList()
        colors.add(ColorTemplate.rgb(getString(color.red)))
        colors.add(ColorTemplate.rgb(getString(color.blue)))
        colors.add(ColorTemplate.rgb(getString(color.orange)))
        colors.add(ColorTemplate.rgb(getString(color.pink)))
        colors.add(ColorTemplate.rgb(getString(color.yellow)))
        colors.add(ColorTemplate.rgb(getString(color.green)))
        return colors
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.menuToolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.menuToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvReportTotalTrans.adapter = reportAdapter
        binding.rvReportTotalTrans.layoutManager = LinearLayoutManager(this)
        binding.rvReportTotalTrans.setHasFixedSize(true)
    }

    private fun setupToggleListener() {
        switchOnOff.setOnCheckedChangeListener { _, checkedId ->
            val radioBtnIncome = binding.incomeBtn
            val radioBtnExpense = binding.expenseBtn

            val textColorIncome = if (checkedId == radioBtnIncome.id) R.color.white else color.blue
            val textColorExpense = if (checkedId == radioBtnExpense.id) R.color.white else color.blue

            radioBtnIncome.setTextColor(ContextCompat.getColor(this, textColorIncome))
            radioBtnExpense.setTextColor(ContextCompat.getColor(this, textColorExpense))

            currentClickedType = if (checkedId == radioBtnIncome.id) "Income" else "Expense"

            chartDataPrepare(currentClickedType)
            displayPieChart(currentClickedType)
            reportAdapter.updateData(
                if (currentClickedType == "Income") incomeTransactionList else expenseTransactionList
            )
        }
    }


    private fun observeTotalAmounts() {
        expenseRepository.incomeTotalAmount?.observe(this) { totalIncome ->
            this.totalIncome = totalIncome
        }

        expenseRepository.expenseTotalAmount?.observe(this) { totalExpense ->
            this.totalExpense = totalExpense
        }
    }

    private fun observeTransactionLists() {
        observeTransactionList(expenseRepository.incomeTransactionListByType, incomeTransactionList)
        observeTransactionList(expenseRepository.expenseTransactionListByType, expenseTransactionList)

        expenseRepository.allTransactionsByDate?.observe(this) { receivedTransactions ->
            if (receivedTransactions.isNotEmpty()) {
                clearAllLists()

                lifecycleScope.launch {
                    receivedTransactions.forEach { transaction ->
                        updateAmountLists(transaction)
                    }
                    chartDataPrepare(currentClickedType)
                    displayPieChart("Income")
                }
            } else {
                Log.d(TAG, "onStart: No data received from observer")
            }
        }
    }


    private fun observeTransactionList(
        liveData: LiveData<List<Expense>>?,
        transactionList: MutableList<Expense>
    ) {
        liveData?.observe(this) { receivedData ->
            if (receivedData.isNotEmpty()) {
                transactionList.clear()
                lifecycleScope.launch {
                    receivedData.forEach { transaction ->
                        transactionList.add(transaction)
                    }
                    reportAdapter.notifyDataSetChanged()
                }
            } else {
                Log.d(TAG, "onStart: No data received from observer")
            }
        }
    }

    private fun clearAllLists() {
        houseIncomeAmountList.clear()
        houseExpenseAmountList.clear()
        foodIncomeAmountList.clear()
        foodExpenseAmountList.clear()
        socialIncomeAmountList.clear()
        socialExpenseAmountList.clear()
        clothesIncomeAmountList.clear()
        clothesExpenseAmountList.clear()
        transIncomeAmountList.clear()
        transExpenseAmountList.clear()
        otherIncomeAmountList.clear()
        otherExpenseAmountList.clear()
    }

    private fun updateAmountListsHelper(
        incomeList: MutableList<Double>,
        expenseList: MutableList<Double>,
        transaction: Expense
    ) {
        if (!transaction.amount.toString().contains("-")) {
            incomeList.add(transaction.amount)
        } else {
            val amount = transaction.amount.toString().replace("-", "")
            expenseList.add(amount.toDouble())
        }
    }

    private fun updateAmountLists(transaction: Expense) {
        when (transaction.category) {
            "House" -> {
                updateAmountListsHelper(houseIncomeAmountList, houseExpenseAmountList, transaction)
            }
            "Food" -> {
                updateAmountListsHelper(foodIncomeAmountList, foodExpenseAmountList, transaction)
            }
            "Social" -> {
                updateAmountListsHelper(socialIncomeAmountList, socialExpenseAmountList, transaction)
            }
            "Clothes" -> {
                updateAmountListsHelper(clothesIncomeAmountList, clothesExpenseAmountList, transaction)
            }
            "Transportation" -> {
                updateAmountListsHelper(transIncomeAmountList, transExpenseAmountList, transaction)
            }
            "Others" -> {
                updateAmountListsHelper(otherIncomeAmountList, otherExpenseAmountList, transaction)
            }
            else -> {
                Log.d(TAG, "No match category")
            }
        }
    }
}


