package com.joinself.sdk.sample

import android.content.Context
import com.joinself.sdk.sample.signin.R
import java.util.Locale

class Utils {
    companion object {
        val fieldOrderList = arrayListOf(
            Constants.CLAIM_KEY_PHOTO,
            Constants.CLAIM_KEY_PHONE,
            Constants.CLAIM_KEY_EMAIL,
            Constants.CLAIM_KEY_NUMBER,
            Constants.CLAIM_KEY_GIVEN_NAME,
            Constants.CLAIM_KEY_SURNAME,
            Constants.CLAIM_KEY_SEX,
            Constants.CLAIM_KEY_DOB,
            Constants.CLAIM_KEY_POB,
            Constants.CLAIM_KEY_ADDRESS,
            Constants.CLAIM_KEY_NATIONALITY,
            Constants.CLAIM_KEY_COUNTRY_OF_ISSUANCE,
            Constants.CLAIM_KEY_DATE_OF_ISSUANCE,
            Constants.CLAIM_KEY_ISSUE_AUTHORITY,
            Constants.CLAIM_KEY_DATE_OF_EXPIRATION,
            Constants.CLAIM_KEY_CATEGORIES,
            Constants.CLAIM_KEY_SORT_CODE,
            Constants.CLAIM_KEY_VALID_FROM,
            Constants.CLAIM_KEY_VALID_TO,
            Constants.FIELD_ACCOUNT_ID,
            Constants.FIELD_NICKNAME
        )

        fun getFactTitleFromKey(context: Context, key: String, source: String?, displayName: String? = null): String {
            if (!displayName.isNullOrEmpty()) return displayName
            var title = ""
            when (key) {

                Constants.CLAIM_KEY_NAME -> {
                    title = context.getString(R.string.name_title)
                }
                Constants.CLAIM_KEY_PHONE -> {
                    title = context.getString(R.string.title_fact_phones)
                }
                Constants.CLAIM_KEY_EMAIL -> {
                    title = context.getString(R.string.title_fact_emails)
                }
                Constants.CLAIM_KEY_NUMBER -> {
                    title =  context.getString(R.string.document_number)
                }
                Constants.CLAIM_KEY_PHOTO -> {
                    title = context.getString(R.string.title_fact_photo)
                }
                Constants.CLAIM_KEY_GIVEN_NAME -> {
                    title = context.getString(R.string.title_fact_givenNames)
                    /*if (source == DocumentType.DRIVING_LICENSE) {
                        title = context.getString(R.string.title_fact_otherNames)
                    } else if ((source == DocumentType.PASSPORT) or (source == DocumentType.IDCARD)){
                        title = context.getString(R.string.title_fact_givenNames)
                    } else{
                        title = context.getString(R.string.title_fact_firstnames)
                    }*/

                }
                Constants.CLAIM_KEY_SURNAME -> {
                    title = context.getString(R.string.title_fact_surname)
                }
                Constants.CLAIM_KEY_DATE_OF_ISSUANCE -> {
                    title = context.getString(R.string.title_fact_doi)
                }
                Constants.CLAIM_KEY_DOB -> {
                    title = context.getString(R.string.title_fact_dob)
                }
                Constants.CLAIM_KEY_DATE_OF_EXPIRATION -> {
                    title = context.getString(R.string.title_fact_doe)
                }
                Constants.CLAIM_KEY_SEX -> {
                    title = context.getString(R.string.title_fact_sex)
                }
                Constants.CLAIM_KEY_POB -> {
                    title = context.getString(R.string.title_fact_pob)
                }
                Constants.CLAIM_KEY_ADDRESS -> {
                    title = context.getString(R.string.title_fact_address)
                }
                Constants.CLAIM_KEY_NATIONALITY -> {
                    title = context.getString(R.string.title_fact_nationality)
                }
                Constants.CLAIM_KEY_ISSUE_AUTHORITY -> {
                    title = context.getString(R.string.title_fact_issue_authority)
                }
                Constants.CLAIM_KEY_CATEGORIES -> {
                    title = context.getString(R.string.title_fact_categories)
                }
                Constants.CLAIM_KEY_MRZ -> {
                    title = context.getString(R.string.title_fact_mrz)
                }
                Constants.CLAIM_KEY_SORT_CODE -> {
                    title = context.getString(R.string.title_fact_sort_node)
                }
                Constants.CLAIM_KEY_VALID_FROM -> {
                    title = context.getString(R.string.title_fact_valid_from)
                }
                Constants.CLAIM_KEY_VALID_TO -> {
                    title = context.getString(R.string.title_fact_valid_to)
                }

                Constants.FIELD_VERIFIED_ON -> {
                    title = context.getString(R.string.verified_on)
                }
                Constants.FIELD_VERIFIED_BY -> {
                    title = context.getString(R.string.verified_by)
                }

                Constants.FIELD_LAST_VERIFIED_ON -> {
                    title = context.getString(R.string.last_verified_on)
                }
                Constants.FIELD_LAST_VERIFIED_BY -> {
                    title = context.getString(R.string.last_verified_by)
                }
                Constants.FIELD_ADDED_ON -> {
                    title = context.getString(R.string.added_on)
                }

                Constants.CLAIM_KEY_COUNTRY_OF_ISSUANCE -> {
                    title = context.getString(R.string.title_fact_country_of_issuance)
                }

                Constants.CLAIM_KEY_SELFIE_VERIFICATION -> {
                    title = context.getString(R.string.title_fact_selfie)
                }

                //social media
                Constants.FIELD_ACCOUNT_ID -> {
                    title = context.getString(R.string.title_fact_account_id)
                }
                Constants.FIELD_NICKNAME -> {
                    title = context.getString(R.string.title_fact_nickname)
                }
                else -> {
                    title = key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
            }

            return title
        }
    }
}