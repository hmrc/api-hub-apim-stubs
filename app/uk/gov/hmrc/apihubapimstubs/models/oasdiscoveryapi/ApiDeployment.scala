/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apihubapimstubs.models.oasdiscoveryapi

import play.api.libs.json.{Format, Json}

import java.time.Instant

case class ApiDeployment(id: String, deploymentTimestamp: Instant)

object ApiDeployment {

  implicit val formatApiDeployment: Format[ApiDeployment] = Json.format[ApiDeployment]

  val cannedResponse: Seq[ApiDeployment] = Seq(
    ApiDeployment("ems-address-weighting-service", Instant.now()),
    ApiDeployment("fake-service-id-1", Instant.now().minusSeconds(1)),
    ApiDeployment("fake-service-id-2", Instant.now().minusSeconds(2))
  )
  
  val oas: String =
    """
      |openapi: 3.0.3
      |info:
      |  title: Validate a P800 Reference.
      |  description: |-
      |    # Usage Terms
      |    These interfaces are business-critical interfaces for HMRC and DWP, supporting thousands of staff - all consumption, or change in consumption levels, should be registered and fully approved (see Registered Consumers below).
      |  contact:
      |    name: HMRC NPS Live Service
      |    url: http://{placeholderforurl}.hmrc.gov.uk
      |    email: user@hmrc.gov.uk
      |  version: 0.0.2
      |  license:
      |    name: HMRC
      |    url: https://license.example.hmrc.gov.uk
      |  x-integration-catalogue:
      |    reviewed-date: 2024-12-12
      |servers:
      |  - url: https://{hostname}:{port}
      |    description: >-
      |      The actual environment values to use will differ depending on the environment.
      |    variables:
      |      hostname:
      |        default: hostname
      |      port:
      |        enum:
      |          - '7008'
      |        default: '7008'
      |tags: []
      |paths:
      |  /nps-json-service/nps/v1/api/reconciliation/p800/{identifier}/{paymentNumber}:
      |    get:
      |      summary: NPS Interface Specification to validate a P800 Reference and retrieve Payment Reference data.
      |      description: |-
      |        # Purpose
      |        This API provides the capability to validate a P800 Reference when provided and if valid, returns the relative Payment Reference details. This endpoint requires Mutual Authentication over TLS 1.2.
      |        - Example URL to NPS:  https://{hostname}:{port}/nps-json-service/nps/v1/api/reconciliation/p800/{identifier}/{paymentNumber} <br>
      |
      |        # Volumes & Registered Consumers
      |        This API is consumed by the following 'Registered Consumers' who would all need to be impacted when a new consumer with an associated new load first registers to use the service, or an uplift is required to the API. Each 'Registered Consumer' below will receive an additional Security Spec. document that outlines how to connect to the various environments as well as any consumer-specific authorisation/authentication details - this is unique to their connection.
      |
      |         | Consumer | Average API Calls Per Hour | Peak API Calls Per Hour | Peak TPS |
      |         |----------|------------------------|---------------------|----------|
      |         | DIGITAL | xxx | xxx | xxx |
      |
      |         *TPS = Transactions per second
      |
      |         ## Version Log
      |         | Version | Date | Author | Description |
      |         |---------|------|--------|-------------|
      |         | 0.0.0 | 15/12/2023 | Modernising Repayments Team | Initial Draft |
      |         | 0.0.1 | 18/12/2023 | Modernising Repayments Team | Updated paymentNumber to be required in path. |
      |         | 0.0.2 | 21/12/2023 | Modernising Repayments Team | URL Refinement & Refactor. |
      |      operationId: P800ReferenceCheck
      |      parameters:
      |        - $ref: '#/components/parameters/CorrelationId'
      |        - $ref: '#/components/parameters/GovUkOriginatorId'
      |        - $ref: '#/components/parameters/Identifier'
      |        - $ref: '#/components/parameters/PaymentNumber'
      |      responses:
      |        '200':
      |          description: Successful Response
      |          headers:
      |            CorrelationId:
      |              $ref: '#/components/headers/CorrelationId'
      |          content:
      |            application/json;charset=UTF-8:
      |              schema:
      |                $ref: '#/components/schemas/paymentReferenceResponse'
      |        '400':
      |          headers:
      |            correlationId:
      |              schema:
      |                $ref: '#/components/schemas/correlationId'
      |              description: A unique ID used for traceability purposes
      |              required: true
      |          description: Bad Request
      |          content:
      |            application/json;charset=UTF-8:
      |              schema:
      |                $ref: '#/components/schemas/errorResponse_400'
      |              examples:
      |                Actual_Response:
      |                  value:
      |                    failures:
      |                      - reason: HTTP message not readable
      |                        code: '400.2'
      |                      - reason: Constraint Violation - Invalid/Missing input parameter
      |                        code: '400.1'
      |        '403':
      |          headers:
      |            correlationId:
      |              schema:
      |                $ref: '#/components/schemas/correlationId'
      |              description: A unique ID used for traceability purposes
      |              required: true
      |          description: 'Forbidden '
      |          content:
      |            application/json;charset=UTF-8:
      |              schema:
      |                $ref: '#/components/schemas/errorResponse_403'
      |              examples:
      |                Actual_Response:
      |                  value:
      |                    reason: Forbidden
      |                    code: '403.2'
      |        '404':
      |          $ref: '#/components/responses/errorResponseNotFound'
      |        '422':
      |          headers:
      |            CorrelationId:
      |              schema:
      |                $ref: '#/components/schemas/correlationId'
      |          description: Unprocessable Entity
      |          content:
      |            application/json;charset=UTF-8:
      |              schema:
      |                $ref: '#/components/schemas/errorResponse'
      |              examples:
      |                Actual_Response:
      |                  value:
      |                    failures:
      |                      - reason: Payment has already been claimed.
      |                        code: '422'
      |        '500':
      |          $ref: '#/components/responses/errorResponseBadGateway'
      |components:
      |  securitySchemes:
      |    basicAuth:
      |      type: http
      |      scheme: basic
      |      description: HTTPS with MTLS1.2
      |  headers:
      |    CorrelationId:
      |      required: true
      |      schema:
      |        $ref: '#/components/schemas/correlationId'
      |  parameters:
      |    CorrelationId:
      |      description:
      |        Correlation ID - used for traceability purposes - note that this
      |        value in the response matches that received in the request to allow correlation.
      |      in: header
      |      name: CorrelationId
      |      required: true
      |      schema:
      |        $ref: '#/components/schemas/correlationId'
      |    GovUkOriginatorId:
      |      description: Identity of the Originating System that made the API call.
      |      in: header
      |      name: gov-uk-originator-id
      |      required: true
      |      schema:
      |        $ref: '#/components/schemas/govUkOriginatorId'
      |    Identifier:
      |      name: identifier
      |      in: path
      |      required: true
      |      description: The identifier of which could be either a Temporary Reference
      |        Number (TRN) or a National Insurance Number (NINO).
      |      schema:
      |        '$ref': '#/components/schemas/identifierParameter'
      |    PaymentNumber:
      |      in: path
      |      name: paymentNumber
      |      required: true
      |      schema:
      |        '$ref': '#/components/schemas/paymentReferenceResponse/properties/paymentNumber'
      |  responses:
      |    errorResponseBadGateway:
      |      headers:
      |        CorrelationId:
      |          schema:
      |            $ref: '#/components/schemas/correlationId'
      |          description: A unique ID used for traceability purposes
      |          required: true
      |      description: Internal Server Error
      |    errorResponseNotFound:
      |      headers:
      |        correlationId:
      |          schema:
      |            $ref: '#/components/schemas/correlationId'
      |          description: A unique ID used for traceability purposes
      |          required: true
      |      description: The requested resource could not be found
      |  schemas:
      |    correlationId:
      |      description:
      |        Correlation ID - used for traceability purposes - note that this
      |        value in the response matches that received in the request to allow correlation.
      |      example: e470d658-99f7-4292-a4a1-ed12c72f1337
      |      format: uuid
      |      type: string
      |    govUkOriginatorId:
      |      description: Identity of the Originating System that made the API call
      |      type: string
      |      enum:
      |        - da2_tbc_digital
      |      example: da2_tbc_digital
      |    errorResponse:
      |      description: Error Response Payload for this API
      |      title: Error Response
      |      type: object
      |      properties:
      |        failures:
      |          '$ref': '#/components/schemas/errorResponseFailure'
      |    errorResponseFailure:
      |      description: Array of Error Response Failure Object in Error Response.
      |      title: Failure Object in Error Response
      |      type: array
      |      items:
      |        '$ref': '#/components/schemas/errorResourceObj'
      |    errorResourceObj:
      |      type: object
      |      required:
      |        - code
      |        - reason
      |      properties:
      |        reason:
      |          minLength: 1
      |          description: Displays the reason of the failure passed from NPS.
      |          type: string
      |          maxLength: 120
      |        code:
      |          minLength: 1
      |          description:
      |            The error code representing the error that has occurred passed
      |            from NPS.
      |          type: string
      |          maxLength: 10
      |    identifierParameter:
      |      description: The identifier of which could be either a Temporary Reference
      |        Number (TRN) e.g. 00A00000 or a National Insurance Number (NINO) e.g. AA000001A.
      |      type: string
      |      pattern: '^(((?:[ACEHJLMOPRSWXY][A-CEGHJ-NPR-TW-Z]|B[A-CEHJ-NPR-TW-Z]|G[ACEGHJ-NPR-TW-Z]|[KT][A-CEGHJ-MPR-TW-Z]|N[A-CEGHJL-NPR-SW-Z]|Z[A-CEGHJ-NPR-TW-Y])[0-9]{6}[A-D]?)|([0-9]{2}[A-Z]{1}[0-9]{5}))$'
      |      example: AA000001A
      |    nationalInsuranceNumber:
      |      title: nationalInsuranceNumber
      |      type: string
      |      minLength: 8
      |      maxLength: 9
      |      pattern: '^((?:[ACEHJLMOPRSWXY][A-CEGHJ-NPR-TW-Z]|B[A-CEHJ-NPR-TW-Z]|G[ACEGHJ-NPR-TW-Z]|[KT][A-CEGHJ-MPR-TW-Z]|N[A-CEGHJL-NPR-SW-Z]|Z[A-CEGHJ-NPR-TW-Y])[0-9]{6}[A-D]?)$'
      |      example: AA000001A
      |    temporaryReferenceNumber:
      |      title: temporaryReferenceNumber
      |      description:
      |        Temporary Reference Number (TRN) - unique for an individual and
      |        used where the individual does not hold a National Insurance Number (NINO)
      |        for whatever reason.
      |      type: string
      |      minLength: 8
      |      maxLength: 8
      |      pattern: '^([0-9]{2}[A-Z]{1}[0-9]{5})$'
      |      example: 00A00000
      |    paymentReferenceResponse:
      |      description: Success response payload for this API.
      |      required:
      |        - reconciliationIdentifier
      |        - paymentNumber
      |        - payeNumber
      |        - taxDistrictNumber
      |        - payableAmount
      |        - paymentAmount
      |      properties:
      |        reconciliationIdentifier:
      |          type: integer
      |          description: The reconciliation identifier for a reconciliation calculation
      |            that occurred on an individual's account.
      |          maximum: 65534
      |          minimum: 1
      |          example: 12
      |        paymentNumber:
      |          type: integer
      |          description: The identifier for the payment.
      |          example: 789
      |          maximum: 2147483646
      |          minimum: 1
      |        payeNumber:
      |          type: string
      |          description: Denotes the PAYE reference number associated with a taxpayer.
      |          example: 123/A56789
      |          maxLength: 10
      |          minLength: 1
      |          pattern: ^[A-Z0-9/ ]+$
      |        taxDistrictNumber:
      |          type: string
      |          description:  unique reference number that identifies where an employer's or pension provider's tax records are stored.
      |          enum:
      |            - FOOBAR
      |        payableAmount:
      |          description: The payable amount of money, required by a taxpayer, benefit scheme, or employer towards National Insurance and PAYE.
      |          example: 10.56
      |          maximum: 99999999999999.98
      |          minimum: -99999999999999.98
      |          multipleOf: 0.01
      |          type: number
      |        paymentAmount:
      |          description: Denotes the total amount for a given payment.
      |          example: 10.56
      |          maximum: 99999999999999.98
      |          minimum: -99999999999999.98
      |          multipleOf: 0.01
      |          type: number
      |    errorResourceObj_400:
      |      type: object
      |      required:
      |        - code
      |        - reason
      |      properties:
      |        reason:
      |          minLength: 1
      |          description: Displays the reason of the failure.
      |          type: string
      |          maxLength: 120
      |        code:
      |          description:
      |            "The error code representing the error that has occurred. Valid
      |            values are
      |400.1 - Constraint violation (Followed by 'Invalid/Missing
      |            input parameter path.to.field'
      |400.2 - HTTP message not readable;"
      |          type: string
      |          enum:
      |            - '400.1'
      |            - '400.2'
      |    errorResponse_400:
      |      description: Error Response Payload for this API
      |      title: Error Response
      |      type: object
      |      properties:
      |        failures:
      |          $ref: '#/components/schemas/errorResponseFailure_400'
      |    errorResponseFailure_400:
      |      description: Array of Error Response Failure Object in Error Response.
      |      title: Failure Object in Error Response
      |      type: array
      |      items:
      |        $ref: '#/components/schemas/errorResourceObj_400'
      |    errorResourceObj_403_Forbidden:
      |      title: 403_Forbidden
      |      type: object
      |      required:
      |        - code
      |        - reason
      |      properties:
      |        reason:
      |          description: Displays the reason of the failure.
      |          type: string
      |          enum:
      |            - Forbidden
      |        code:
      |          description: 'The error code representing the Forbidden Error. '
      |          type: string
      |          enum:
      |            - '403.2'
      |    errorResponse_403:
      |      oneOf:
      |        - $ref: '#/components/schemas/errorResourceObj_403_Forbidden'
      |      description: Error Response Payload for this API
      |      title: Forbidden Error Response
      |""".stripMargin 

}
