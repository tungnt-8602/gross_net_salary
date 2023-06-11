package com.example.grossnetsalary

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.grossnetsalary.model.BaseSalary
import com.example.grossnetsalary.model.Deduction
import com.example.grossnetsalary.model.Insurance
import com.example.grossnetsalary.model.Item
import com.example.grossnetsalary.model.MinZoneSalary
import com.example.grossnetsalary.model.PersonalIncomeTax
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.util.Locale


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getInputAndCal()

    }

    private fun getInputAndCal(){
        val grossText = findViewById<TextView>(R.id.grossText)
        val grossValue = findViewById<EditText>(R.id.grossValue)
        grossText.setOnClickListener {
            grossValue.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(grossValue, InputMethodManager.SHOW_IMPLICIT)
        }
        var gv = 0
        grossValue?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                gv = validateIntInput(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                gv = validateIntInput(s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                gv = validateIntInput(s.toString())
            }
        })

        var salaryOnInsurance = validateIntInput(grossValue.text.toString())
        val otherEditText = findViewById<EditText>(R.id.otherValue)
        val iso = findViewById<RadioGroup>(R.id.insuranceSalaryOption)
        iso.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == findViewById<RadioButton>(R.id.other).id) {
                otherEditText?.visibility = View.VISIBLE
                otherEditText?.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(otherEditText, InputMethodManager.SHOW_IMPLICIT)
                otherEditText?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        salaryOnInsurance = validateIntInput(s.toString())
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        salaryOnInsurance = validateIntInput(grossValue?.text.toString())
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }
                })

            } else {
                otherEditText?.visibility = View.GONE
                salaryOnInsurance = 1
            }
        }

        val dependentText = findViewById<TextView>(R.id.dependentPersonText)
        val dependentNum = findViewById<EditText>(R.id.dependentPersonValue)
        dependentText.setOnClickListener {
            dependentNum.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(dependentNum, InputMethodManager.SHOW_IMPLICIT)
        }
        var dn = 0
        dependentNum?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dn = validateIntInput(dependentNum.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val spinner = findViewById<Spinner>(R.id.zoneSpinner)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.zone_list, android.R.layout.select_dialog_item
        )
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice)
        spinner.adapter = adapter

        var selectedZone = 1
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedZone = parent.getItemAtPosition(position).toString().toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedZone = 1
            }
        }

        val netcalbutton = findViewById<Button>(R.id.netButton)
        netcalbutton.setOnClickListener {
            calculateNet(gv, salaryOnInsurance, dn, selectedZone)
        }

        val grosscalbutton = findViewById<Button>(R.id.grossbutton)
        grosscalbutton.setOnClickListener {
            calculateGross(gv, salaryOnInsurance, dn, selectedZone)
        }
    }


    @SuppressLint("SetTextI18n")
    fun calculateNet(
        grossValue: Int,
        salaryOnInsurance: Int,
        numOfDepender: Int,
        selectedZone: Int
    ) {
        val insurances: List<Insurance> = convertJsonToObject(this).insurances
        val deductions: List<Deduction> = convertJsonToObject(this).deductions
        val minZoneSalaries: List<MinZoneSalary> = convertJsonToObject(this).minZoneSalaries
        val personalIncomeTaxes: List<PersonalIncomeTax> =
            convertJsonToObject(this).personalIncomeTaxes
        val baseSalaries: List<BaseSalary> = convertJsonToObject(this).baseSalaries

        var net = 0.0
        var minSalary = 0
        var bhxh = 0.0
        var bhyt = 0.0
        var bhtn = 0.0
        val tntt: Double
        val gcbt: Int
        val gcnpt: Int
        val tnct: Double
        var ttncn = 0.0
        val res: Double
        var maxBhtn = 0

        //Tính lương tối đa đóng bảo hiêm thất nghiệp theo vùng
        var insuranceRes: Double
        for (minZoneSalary in minZoneSalaries) {
            for (minInZone in minZoneSalary.value) {
                if (minInZone.zone == selectedZone) {
                    minSalary = minInZone.value
                    maxBhtn = minInZone.maxbhtn
                }
            }
        }
        //validate input
        if (grossValue <= 0) {
            android.widget.Toast.makeText(
                this,
                "Vui lòng nhập lương tổng hợp lệ ",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (salaryOnInsurance <= 0) {
            android.widget.Toast.makeText(
                this,
                "Vui lòng chọn mức lương đóng bảo hiểm hợp lệ",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        val salaryOnInsuranceValue: Int = if (salaryOnInsurance == 1) {
            grossValue
        } else {
            salaryOnInsurance
        }
        if (grossValue < minSalary) {
            android.widget.Toast.makeText(
                this,
                "Lương gross phải lớn hơn mức lương tối thiểu vùng: $minSalary",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            var iRate = 0.0
            var hRate = 0.0
            var bhtnrate = 0.0
            var orate = 0.0
            for (insurance in insurances) {
                if (insurance.title.contains("thất nghiệp", ignoreCase = true)) {
                    bhtnrate += insurance.value
                } else if (insurance.title.contains("y tế", ignoreCase = true)) {
                    hRate += insurance.value
                } else if (insurance.title.contains("xã hội", ignoreCase = true)) {
                    iRate += insurance.value
                } else {
                    orate += insurance.value
                }
            }
            //Tính tiền bảo hiểm
            for (baseSalary in baseSalaries) {
                //lương đóng bảo hiểm nhỏ hơn mức lương tối đa đóng bảo hiểm(< 29,800,000) -> tính theo công thức, tỉ lệ bthg
                if (salaryOnInsuranceValue <= baseSalary.value.number * baseSalary.value.rate) {
                    bhxh = salaryOnInsuranceValue * iRate / 100
                    bhyt = salaryOnInsuranceValue * hRate / 100
                    bhtn = salaryOnInsuranceValue * bhtnrate / 100
                    insuranceRes = bhxh + bhyt + bhtn
                    net = grossValue - insuranceRes
                }//nếu lương đóng bảo hiểm lớn hơn mức lương tối đa đóng bảo hiểm thất nghiệp theo vùng(> 65 củ -> 93.6 củ)
                else if (salaryOnInsuranceValue > maxBhtn) {
                    bhxh = baseSalary.value.number * baseSalary.value.rate * iRate / 100
                    bhyt = baseSalary.value.number * baseSalary.value.rate * hRate / 100
                    bhtn = maxBhtn * bhtnrate / 100
                    insuranceRes = bhxh + bhyt + bhtn
                    net = grossValue - insuranceRes
                }
                //29.8 củ < ương đóng bảo hiểm < 65-93.6 củ
                else {
                    bhxh = baseSalary.value.number * baseSalary.value.rate * iRate / 100
                    bhyt = baseSalary.value.number * baseSalary.value.rate * hRate / 100
                    bhtn = salaryOnInsuranceValue * bhtnrate / 100
                    insuranceRes = bhxh + bhyt + bhtn
                    net = grossValue - insuranceRes
                }
            }

            //Thu nhập trước thuế
            tntt = net
            gcbt = deductions[0].value
            gcnpt = numOfDepender * deductions[1].value
            if (net > (deductions[0].value + numOfDepender * deductions[1].value) ){
                net -= (gcbt + gcnpt)
            } else {
                net = 0.0
            }

            //Thu nhập chịu thuế = tntt - các khoản được giảm trừ
            tnct = net
            var tnctTmp = tnct

            //Tính thuế thu nhập cá nhân
            for (personalIncomeTax in personalIncomeTaxes) {
                for (value in personalIncomeTax.value) {
                    if (net > (value.min ?: Double.MIN_VALUE)) {
                        if (net > (value.max ?: Double.MAX_VALUE)) {
                            ttncn += (value.max!! - (value.min
                                ?: Double.MIN_VALUE)) * value.ratio / 100
                            if (tnctTmp > value.max) {
                                tnctTmp -= value.max
                            }
                        } else {
                            ttncn += ((net - (value.min ?: Double.MIN_VALUE)) * value.ratio / 100)
                            ttncn += 0
                            break
                        }
                    }
                }
            }

            //kết quả cuối cùng
            res = tntt - ttncn
        }

        if (res != 0.0) {
            showResult(1, grossValue, bhxh, bhyt, bhtn, tntt, gcbt, gcnpt, ttncn, tnct, res)
        }
    }

    @SuppressLint("SetTextI18n")
    fun calculateGross(
        netValue: Int,
        salaryOnInsurance: Int,
        numOfDepender: Int,
        selectedZone: Int
    ) {
        val insurances: List<Insurance> = convertJsonToObject(this).insurances
        val deductions: List<Deduction> = convertJsonToObject(this).deductions
        val minZoneSalaries: List<MinZoneSalary> = convertJsonToObject(this).minZoneSalaries
        val personalIncomeTaxes: List<PersonalIncomeTax> =
            convertJsonToObject(this).personalIncomeTaxes
        val baseSalaries: List<BaseSalary> = convertJsonToObject(this).baseSalaries

        var gross = 0.0
        var minSalary = 0
        var bhxh = 0.0
        var bhyt = 0.0
        var bhtn = 0.0

        val tntt: Double
        var tnct = 0.0
        val ttncn: Double
        var maxBhtn = 0

        val ttqd: Double
        var taxValueStack = 0.0

        //Tính lương tối đa đóng bảo hiêm thất nghiệp theo vùng
        var insuranceRes: Double
        for (minZoneSalary in minZoneSalaries) {
            for (minInZone in minZoneSalary.value) {
                if (minInZone.zone == selectedZone) {
                    minSalary = minInZone.value
                    maxBhtn = minInZone.maxbhtn
                }
            }
        }

        //validate input
        if (netValue <= 0) {
            android.widget.Toast.makeText(
                this,
                "Vui lòng nhập lương net hợp lệ ",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (salaryOnInsurance <= 0) {
            android.widget.Toast.makeText(
                this,
                "Vui lòng chọn mức lương đóng bảo hiểm hợp lệ",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        val gcbt: Int = deductions[0].value
        val gcnpt: Int = numOfDepender * deductions[1].value
        ttqd = if (netValue > (deductions[0].value + numOfDepender * deductions[1].value)) {
            (netValue - (gcbt + gcnpt)).toDouble()
        } else {
            0.0
        }
        for (personalIncomeTax in personalIncomeTaxes) {
            for (value in personalIncomeTax.value) {
                if (value.max != null) {
                    if (ttqd + taxValueStack + ((value.max) - (value.min
                            ?: Double.MIN_VALUE)) * value.ratio / 100 > (value.min
                            ?: Double.MIN_VALUE)
                    ) {
                        if (ttqd + taxValueStack + ((value.max) - (value.min
                                ?: Double.MIN_VALUE)) * value.ratio / 100 > (value.max)
                        ) {
                            taxValueStack += ((value.max) - (value.min
                                ?: Double.MIN_VALUE)) * value.ratio / 100
                            taxValueStack += 0
                        } else {
                            tnct = (ttqd + taxValueStack - (value.min
                                ?: Double.MIN_VALUE) * (value.ratio) / 100) / (1 - (value.ratio) / 100)
                            break
                        }
                    }
                } else {
                    tnct = (ttqd + taxValueStack - (value.min
                        ?: Double.MIN_VALUE) * (value.ratio) / 100) / (1 - (value.ratio) / 100)
                }
            }
        }
        tnct += 0.0
        ttncn = tnct - ttqd
        tntt = netValue + ttncn

        var iRate = 0.0
        var hRate = 0.0
        var bhtnrate = 0.0
        for (insurance in insurances) {
            if (insurance.title.contains("thất nghiệp", ignoreCase = true)) {
                bhtnrate += insurance.value
            } else if (insurance.title.contains("y tế", ignoreCase = true)) {
                hRate += insurance.value
            } else if (insurance.title.contains("xã hội", ignoreCase = true)) {
                iRate += insurance.value
            }
        }

        if (salaryOnInsurance != 1) {
            for (baseSalary in baseSalaries) {
                //lương đóng bảo hiểm nhỏ hơn mức lương tối đa đóng bảo hiểm(< 29,800,000) -> tính theo công thức, tỉ lệ bthg
                if (salaryOnInsurance <= baseSalary.value.number * baseSalary.value.rate) {
                    bhxh = salaryOnInsurance * iRate / 100
                    bhyt = salaryOnInsurance * hRate / 100
                    bhtn = salaryOnInsurance * bhtnrate / 100
                    insuranceRes = bhxh + bhyt + bhtn
                    gross = tntt + insuranceRes
                }//nếu lương đóng bảo hiểm lớn hơn mức lương tối đa đóng bảo hiểm thất nghiệp theo vùng(> 65 củ -> 93.6 củ)
                else if (salaryOnInsurance > maxBhtn) {
                    bhxh = baseSalary.value.number * baseSalary.value.rate * iRate / 100
                    bhyt = baseSalary.value.number * baseSalary.value.rate * hRate / 100
                    bhtn = maxBhtn * bhtnrate / 100
                    insuranceRes = bhxh + bhyt + bhtn
                    gross = tntt + insuranceRes
                }
                //29.8 củ < lương đóng bảo hiểm < 65-93.6 củ
                else {
                    bhxh = baseSalary.value.number * baseSalary.value.rate * iRate / 100
                    bhyt = baseSalary.value.number * baseSalary.value.rate * hRate / 100
                    bhtn = salaryOnInsurance * bhtnrate / 100
                    insuranceRes = bhxh + bhyt + bhtn
                    gross = tntt + insuranceRes
                }
            }
        } else {
            for (baseSalary in baseSalaries) {
                //lương đóng bảo hiểm nhỏ hơn mức lương tối đa đóng bảo hiểm(< 29,800,000) -> tính theo công thức, tỉ lệ bthg
                if (tntt <= baseSalary.value.number * baseSalary.value.rate * (1 - (iRate + hRate + bhtnrate) / 100)) {
                    gross = tntt / ((1 - (iRate + hRate + bhtnrate) / 100))
                    bhxh = gross * iRate / 100
                    bhyt = gross * hRate / 100
                    bhtn = gross * bhtnrate / 100
                }//nếu lương đóng bảo hiểm lớn hơn mức lương tối đa đóng bảo hiểm thất nghiệp theo vùng(> 65 củ -> 93.6 củ)
                else if (tntt + maxBhtn * bhtnrate / 100 + baseSalary.value.number * baseSalary.value.rate * (iRate + hRate) / 100 > maxBhtn) {
                    bhxh = baseSalary.value.number * baseSalary.value.rate * iRate / 100
                    bhyt = baseSalary.value.number * baseSalary.value.rate * hRate / 100
                    bhtn = maxBhtn * bhtnrate / 100
                    insuranceRes = bhxh + bhyt + bhtn
                    gross = tntt + insuranceRes
                }
                //29.8 củ < lương đóng bảo hiểm < 65-93.6 củ
                else {
                    bhxh = baseSalary.value.number * baseSalary.value.rate * iRate / 100
                    bhyt = baseSalary.value.number * baseSalary.value.rate * hRate / 100
                    gross = (tntt + bhxh + bhyt) / (1 - bhtnrate / 100)
                    bhtn = (gross * bhtnrate) / 100
                }
            }
        }
        if(gross < minSalary){
            android.widget.Toast.makeText(
                this,
                "Không thể tính ra được lương gross lớn hơn lương tối thiểu vùng",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (gross != 0.0) {
            showResult(2, netValue, bhxh, bhyt, bhtn, tntt, gcbt, gcnpt, ttncn, tnct, gross)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showResult(type: Int, input: Int, bhxh: Double, bhyt: Double, bhtn: Double, tntt: Double, gcbt: Int, gcnpt: Int, ttncn: Double, tnct: Double, res: Double) {
        val formatterVN = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        val resultView = findViewById<ConstraintLayout>(R.id.resultView)
        val grossResult = findViewById<TextView>(R.id.grossResult)
        val insuranceResult = findViewById<TextView>(R.id.insuranceResult)
        val pitaxesResult = findViewById<TextView>(R.id.pitaxesResult)
        val netResult = findViewById<TextView>(R.id.netResult)
        val netRow = findViewById<TableRow>(R.id.netRow)
        val grossRow = findViewById<TableRow>(R.id.grossRow)
        val netButton = findViewById<Button>(R.id.netButton)
        val detailButton = findViewById<Button>(R.id.detailButton)

        resultView.visibility = View.VISIBLE

        if (type == 1) {
            grossResult.text = formatterVN.format(input)
            insuranceResult.text = "- ${formatterVN.format(bhxh + bhyt + bhtn)}"
            pitaxesResult.text = "- ${formatterVN.format(ttncn)}"
            netResult.text = formatterVN.format(res)

            netRow.setBackgroundColor(Color.Green.hashCode())
            grossRow.setBackgroundColor(Color.White.hashCode())

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(netButton.windowToken, 0)

            detailButton.setOnClickListener {
                showDetailDialog(formatterVN, input.toDouble(), bhxh, bhyt, bhtn, tntt, gcbt, gcnpt, ttncn, tnct, res)
            }
        } else {
            grossResult.text = formatterVN.format(res)
            insuranceResult.text = "- ${formatterVN.format(bhxh + bhyt + bhtn)}"
            pitaxesResult.text = "- ${formatterVN.format(ttncn)}"
            netResult.text = formatterVN.format(input)

            netRow.setBackgroundColor(Color.White.hashCode())
            grossRow.setBackgroundColor(Color.Green.hashCode())

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(netButton.windowToken, 0)

            detailButton.setOnClickListener {
                showDetailDialog(formatterVN, res, bhxh, bhyt, bhtn, tntt, gcbt, gcnpt, ttncn, tnct, input.toDouble()
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDetailDialog(formatterVN: NumberFormat, grossVal: Double, bhxh: Double, bhyt: Double, bhtn: Double, tntt: Double, gcbt: Int, gcnpt: Int, ttncn: Double, tnct: Double, netValue: Double) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Diễn giải chi tiết")
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_insurance_detail, null)
        builder.setView(dialogView)
            .setPositiveButton(R.string.ok) { dialog, id -> }

        dialogView.findViewById<TextView>(R.id.grossVal).text = formatterVN.format(grossVal)
        dialogView.findViewById<TextView>(R.id.bhxhValue).text = "- ${formatterVN.format(bhxh)}"
        dialogView.findViewById<TextView>(R.id.bhytValue).text = "- ${formatterVN.format(bhyt)}"
        dialogView.findViewById<TextView>(R.id.bhtnValue).text = "- ${formatterVN.format(bhtn)}"
        dialogView.findViewById<TextView>(R.id.tnttValue).text = formatterVN.format(tntt)
        dialogView.findViewById<TextView>(R.id.gcbtValue).text = "- ${formatterVN.format(gcbt)}"
        dialogView.findViewById<TextView>(R.id.gcnptValue).text = "- ${formatterVN.format(gcnpt)}"
        dialogView.findViewById<TextView>(R.id.ttncnValue).text = "- ${formatterVN.format(ttncn)}"
        dialogView.findViewById<TextView>(R.id.tnctValue).text = formatterVN.format(tnct)
        dialogView.findViewById<TextView>(R.id.netValue).text = formatterVN.format(netValue)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun convertJsonToObject(context: Context): ConvertedData {
        val gson = Gson()
        val jsonString =
            context.assets.open("salary.json").bufferedReader().use { it.readText() }
        val listTax = object : TypeToken<ArrayList<Item>>() {}.type
        val items: ArrayList<Item> = gson.fromJson(jsonString, listTax)

        var part1 = "["
        var part2 = "["
        var part3 = "["
        var part4 = "["
        var part5 = "["

        for (item in items) {
            when (item.type) {
                1 -> part1 += gson.toJson(item) + ","
                2 -> part2 += gson.toJson(item) + ","
                3 -> part3 += gson.toJson(item) + ","
                4 -> part4 += gson.toJson(item) + ","
                5 -> part5 += gson.toJson(item) + ","
            }
        }
        part1 = customString(part1)
        part2 = customString(part2)
        part3 = customString(part3)
        part4 = customString(part4)
        part5 = customString(part5)

        val insurances: List<Insurance> =
            gson.fromJson(part1, object : TypeToken<List<Insurance>>() {}.type)
        val deductions: List<Deduction> =
            gson.fromJson(part2, object : TypeToken<List<Deduction>>() {}.type)
        val minZoneSalarys: List<MinZoneSalary> =
            gson.fromJson(part3, object : TypeToken<List<MinZoneSalary>>() {}.type)
        val personalIncomeTaxs: List<PersonalIncomeTax> =
            gson.fromJson(part4, object : TypeToken<List<PersonalIncomeTax>>() {}.type)
        val baseSalaries: List<BaseSalary> =
            gson.fromJson(part5, object : TypeToken<List<BaseSalary>>() {}.type)

        return ConvertedData(
            insurances,
            deductions,
            minZoneSalarys,
            personalIncomeTaxs,
            baseSalaries
        )
    }

    data class ConvertedData(
        val insurances: List<Insurance>,
        val deductions: List<Deduction>,
        val minZoneSalaries: List<MinZoneSalary>,
        val personalIncomeTaxes: List<PersonalIncomeTax>,
        val baseSalaries: List<BaseSalary>
    )

    fun validateIntInput(str: String): Int {
        return if (str == "") {
            0
        } else {
            str.toInt()
        }
    }

    private fun customString(input: String): String {
        return input.run {
            if (endsWith(",")) {
                dropLast(1) + "]"
            } else {
                this
            }
        }
    }
}
