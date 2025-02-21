package com.example.grossnetsalary

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import com.example.grossnetsalary.databinding.ActivityMainBinding
import com.example.grossnetsalary.databinding.DialogInsuranceDetailBinding
import com.example.grossnetsalary.model.BaseSalary
import com.example.grossnetsalary.model.Deduction
import com.example.grossnetsalary.model.Insurance
import com.example.grossnetsalary.model.MinZoneSalary
import com.example.grossnetsalary.model.PersonalIncomeTax
import com.example.grossnetsalary.model.ResultShow
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getInputAndCal()
    }

    private fun getInputAndCal() {
        var gv = 0
        val grossValue = binding.grossValue

        binding.grossText.setOnClickListener {
            grossValue.showKeyboard()
        }
        grossValue.addTextChangedListener(object : TextWatcher {
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
        val otherEditText = binding.otherValue
        binding.insuranceSalaryOption.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.other.id) {
                otherEditText.visibility = View.VISIBLE
                otherEditText.showKeyboard()
                otherEditText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        salaryOnInsurance = validateIntInput(s.toString())
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        salaryOnInsurance = validateIntInput(grossValue.text.toString())
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
                otherEditText.visibility = View.GONE
                salaryOnInsurance = 1
            }
        }

        val dependentNum = binding.dependentPersonValue
        binding.dependentPersonText.setOnClickListener {
            dependentNum.showKeyboard()
        }
        var dn = 0
        dependentNum.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dn = validateIntInput(dependentNum.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val spinner = binding.zoneSpinner
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

        binding.netButton.setOnClickListener {
            calculateNet(gv, salaryOnInsurance, dn, selectedZone)
        }

        binding.grossbutton.setOnClickListener {
            calculateGross(gv, salaryOnInsurance, dn, selectedZone)
        }
    }

    private fun calculateNet(
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
            Toast.makeText(
                this,
                "Vui lòng nhập lương tổng hợp lệ ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (salaryOnInsurance <= 0) {
            Toast.makeText(
                this,
                "Vui lòng chọn mức lương đóng bảo hiểm hợp lệ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val salaryOnInsuranceValue: Int = if (salaryOnInsurance == 1) {
            grossValue
        } else {
            salaryOnInsurance
        }
        if (grossValue < minSalary) {
            Toast.makeText(
                this,
                "Lương gross phải lớn hơn mức lương tối thiểu vùng: $minSalary",
                Toast.LENGTH_SHORT
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
            if (net > (deductions[0].value + numOfDepender * deductions[1].value)) {
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
            showResult(
                ResultShow(
                    1,
                    grossValue,
                    bhxh,
                    bhyt,
                    bhtn,
                    tntt,
                    gcbt,
                    gcnpt,
                    ttncn,
                    tnct,
                    res
                )
            )
        }
    }

    private fun calculateGross(
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
            Toast.makeText(
                this,
                "Vui lòng nhập lương net hợp lệ ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (salaryOnInsurance <= 0) {
            Toast.makeText(
                this,
                "Vui lòng chọn mức lương đóng bảo hiểm hợp lệ",
                Toast.LENGTH_SHORT
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
        if (gross < minSalary) {
            Toast.makeText(
                this,
                "Không thể tính ra được lương gross lớn hơn lương tối thiểu vùng",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (gross != 0.0) {
            showResult(
                ResultShow(
                    2,
                    netValue,
                    bhxh,
                    bhyt,
                    bhtn,
                    tntt,
                    gcbt,
                    gcnpt,
                    ttncn,
                    tnct,
                    gross
                )
            )
        }
    }

    private fun showResult(resultShow: ResultShow) {
        val formatterVN = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        val resultView = binding.resultView
        val grossResult = binding.grossResult
        val insuranceResult = binding.insuranceResult
        val pitaxesResult = binding.pitaxesResult
        val netResult = binding.netResult
        val netRow = binding.netRow
        val grossRow = binding.grossRow
        val detailButton = binding.detailButton

        resultView.visibility = View.VISIBLE

        if (resultShow.type == 1) {
            grossResult.text = formatterVN.format(resultShow.input)
            insuranceResult.text = getString(
                R.string.start_dash_str,
                formatterVN.format(resultShow.bhxh + resultShow.bhyt + resultShow.bhtn)
            )
            pitaxesResult.text =
                getString(R.string.start_dash_str, formatterVN.format(resultShow.ttncn))
            netResult.text = formatterVN.format(resultShow.res)

            netRow.setBackgroundColor(Color.Green.hashCode())
            grossRow.setBackgroundColor(Color.White.hashCode())

            binding.root.hideKeyboard()

            detailButton.setOnClickListener {
                showDetailDialog(
                    formatterVN,
                    ResultShow(
                        1, resultShow.input,
                        resultShow.bhxh,
                        resultShow.bhyt,
                        resultShow.bhtn,
                        resultShow.tntt,
                        resultShow.gcbt,
                        resultShow.gcnpt,
                        resultShow.ttncn,
                        resultShow.tnct,
                        resultShow.res
                    )
                )
            }
        } else {
            grossResult.text = formatterVN.format(resultShow.res)
            insuranceResult.text = getString(
                R.string.start_dash_str,
                formatterVN.format(resultShow.bhxh + resultShow.bhyt + resultShow.bhtn)
            )
            pitaxesResult.text =
                getString(R.string.start_dash_str, formatterVN.format(resultShow.ttncn))
            netResult.text = formatterVN.format(resultShow.input)

            netRow.setBackgroundColor(Color.White.hashCode())
            grossRow.setBackgroundColor(Color.Green.hashCode())

            binding.root.hideKeyboard()

            detailButton.setOnClickListener {
                showDetailDialog(
                    formatterVN,
                    ResultShow(
                        1, resultShow.res.toInt(),
                        resultShow.bhxh,
                        resultShow.bhyt,
                        resultShow.bhtn,
                        resultShow.tntt,
                        resultShow.gcbt,
                        resultShow.gcnpt,
                        resultShow.ttncn,
                        resultShow.tnct,
                        resultShow.input.toDouble()
                    )
                )
            }
        }
    }

    private fun showDetailDialog(
        formatterVN: NumberFormat,
        resultShow: ResultShow,
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Diễn giải chi tiết")

        val dialogBinding =
            DialogInsuranceDetailBinding.inflate(layoutInflater) // Inflate using binding
        builder.setView(dialogBinding.root) // Set the root of the binding layout

        builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }

        dialogBinding.grossVal.text = formatterVN.format(resultShow.input)
        dialogBinding.bhxhValue.text =
            getString(R.string.start_dash_str, formatterVN.format(resultShow.bhxh))
        dialogBinding.bhytValue.text =
            getString(R.string.start_dash_str, formatterVN.format(resultShow.bhyt))
        dialogBinding.bhtnValue.text =
            getString(R.string.start_dash_str, formatterVN.format(resultShow.bhtn))
        dialogBinding.tnttValue.text = formatterVN.format(resultShow.tntt)
        dialogBinding.gcbtValue.text =
            getString(R.string.start_dash_str, formatterVN.format(resultShow.gcbt))
        dialogBinding.gcnptValue.text =
            getString(R.string.start_dash_str, formatterVN.format(resultShow.gcnpt))
        dialogBinding.ttncnValue.text =
            getString(R.string.start_dash_str, formatterVN.format(resultShow.ttncn))
        dialogBinding.tnctValue.text = formatterVN.format(resultShow.tnct)
        dialogBinding.netValue.text = formatterVN.format(resultShow.res)

        builder.create().show()
    }
}