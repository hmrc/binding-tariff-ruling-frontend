/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.bindingtariffrulingfrontend.metrics

import com.codahale.metrics.Timer
import com.codahale.metrics.MetricRegistry
import org.mockito.ArgumentMatchers.*
import org.mockito.{InOrder, Mockito}
import org.mockito.Mockito.{mock, times, verifyNoMoreInteractions, when}
import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatest.{BeforeAndAfterAll, OptionValues}
import play.api.mvc.{MessagesAbstractController, Results}
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.Future

class HasMetricsSpec extends AsyncWordSpecLike with Matchers with OptionValues with BeforeAndAfterAll {

  trait MockHasMetrics { self: HasMetrics =>
    val timer: Timer.Context                = mock(classOf[Timer.Context])
    val metrics: MetricRegistry             = mock(classOf[MetricRegistry])
    override val localMetrics: LocalMetrics = mock(classOf[LocalMetrics])
    when(localMetrics.startTimer(anyString())).thenReturn(timer)
  }

  class TestHasMetrics extends HasMetrics with MockHasMetrics

  class TestHasActionMetrics
      extends MessagesAbstractController(Helpers.stubMessagesControllerComponents())
      with HasActionMetrics
      with MockHasMetrics

  def withTestMetrics[A](test: TestHasMetrics => A): A =
    test(new TestHasMetrics)

  def withTestActionMetrics[A](test: TestHasActionMetrics => A): A =
    test(new TestHasActionMetrics)

  def verifyCompletedWithSuccess(metricName: String, metrics: MockHasMetrics): Assertion = {
    val inOrder: InOrder = Mockito.inOrder(metrics.localMetrics, metrics.timer)
    inOrder.verify(metrics.localMetrics, times(1)).startTimer(metricName)
    inOrder.verify(metrics.localMetrics, times(1)).stopTimer(metrics.timer)
    inOrder.verify(metrics.localMetrics, times(1)).incrementSuccessCounter(metricName)
    verifyNoMoreInteractions(metrics.localMetrics)
    verifyNoMoreInteractions(metrics.timer)
    succeed
  }

  def verifyCompletedWithFailure(metricName: String, metrics: MockHasMetrics): Assertion = {
    val inOrder = Mockito.inOrder(metrics.localMetrics, metrics.timer)
    inOrder.verify(metrics.localMetrics, times(1)).startTimer(metricName)
    inOrder.verify(metrics.localMetrics, times(1)).stopTimer(metrics.timer)
    inOrder.verify(metrics.localMetrics, times(1)).incrementFailedCounter(metricName)
    verifyNoMoreInteractions(metrics.localMetrics)
    verifyNoMoreInteractions(metrics.timer)
    succeed
  }

  val TestMetric = "test-metric"

  "HasMetrics" when {
    "withMetricsTimerAsync" should {
      "increment success counter for a successful future" in withTestMetrics { metrics =>
        metrics.withMetricsTimerAsync(TestMetric)(_ => Future.successful(())).map { _ =>
          verifyCompletedWithSuccess(TestMetric, metrics)
        }
      }

      "increment success counter for a successful future where completeWithSuccess is called explicitly" in withTestMetrics {
        metrics =>
          metrics
            .withMetricsTimerAsync(TestMetric) { timer =>
              timer.completeWithSuccess()
              Future.successful(())
            }
            .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "increment failure counter for a failed future" in withTestMetrics { metrics =>
        metrics.withMetricsTimerAsync(TestMetric)(_ => Future.failed(new Exception)).recover { case _ =>
          verifyCompletedWithFailure(TestMetric, metrics)
        }
      }

      "increment failure counter for a successful future where completeWithFailure is called explicitly" in withTestMetrics {
        metrics =>
          metrics
            .withMetricsTimerAsync(TestMetric) { timer =>
              timer.completeWithFailure()
              Future.successful(())
            }
            .map(_ => verifyCompletedWithFailure(TestMetric, metrics))
      }

      "only increment counters once regardless of how many times the user calls complete with success" in withTestMetrics {
        metrics =>
          metrics
            .withMetricsTimerAsync(TestMetric) { timer =>
              Future(timer.completeWithSuccess())
              Future(timer.completeWithSuccess())
              Future(timer.completeWithSuccess())
              Future(timer.completeWithSuccess())
              Future.successful(())
            }
            .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "only increment counters once regardless of how many times the user calls complete with failure" in withTestMetrics {
        metrics =>
          metrics
            .withMetricsTimerAsync(TestMetric) { timer =>
              Future(timer.completeWithFailure())
              Future(timer.completeWithFailure())
              Future(timer.completeWithFailure())
              Future(timer.completeWithFailure())
              timer.completeWithFailure()
              Future.successful(())
            }
            .map(_ => verifyCompletedWithFailure(TestMetric, metrics))
      }

      "increment failure counter when the user throws an exception constructing their code block" in withTestMetrics {
        metrics =>
          assertThrows[RuntimeException] {
            metrics.withMetricsTimerAsync(TestMetric)(_ => throw new RuntimeException)
          }

          Future.successful(verifyCompletedWithFailure(TestMetric, metrics))
      }
    }

    "withMetricsTimerResult" should {
      "increment success counter for a successful future of an informational Result" in withTestMetrics { metrics =>
        metrics
          .withMetricsTimerResult(TestMetric) {
            Future.successful(Results.Continue)
          }
          .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "increment success counter for a successful future of a successful Result" in withTestMetrics { metrics =>
        metrics
          .withMetricsTimerResult(TestMetric) {
            Future.successful(Results.Ok)
          }
          .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "increment success counter for a successful future of a redirect Result" in withTestMetrics { metrics =>
        metrics
          .withMetricsTimerResult(TestMetric) {
            Future.successful(Results.NotModified)
          }
          .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "increment failure counter for a successful future of a client error Result" in withTestMetrics { metrics =>
        metrics
          .withMetricsTimerResult(TestMetric) {
            Future.successful(Results.EntityTooLarge)
          }
          .map(_ => verifyCompletedWithFailure(TestMetric, metrics))
      }

      "increment failure counter for a successful future of a server error Result" in withTestMetrics { metrics =>
        metrics
          .withMetricsTimerResult(TestMetric) {
            Future.successful(Results.BadGateway)
          }
          .map(_ => verifyCompletedWithFailure(TestMetric, metrics))
      }

      "increment failure counter for a failed future" in withTestMetrics { metrics =>
        metrics
          .withMetricsTimerResult(TestMetric) {
            Future.failed(new Exception)
          }
          .transformWith(_ => verifyCompletedWithFailure(TestMetric, metrics))
      }

      "increment failure counter when the user throws an exception constructing their code block" in withTestMetrics {
        metrics =>
          assertThrows[RuntimeException] {
            metrics.withMetricsTimerResult(TestMetric) {
              throw new RuntimeException
            }
          }

          Future.successful(verifyCompletedWithFailure(TestMetric, metrics))
      }
    }

    "withMetricsTimerAction" should {
      def fakeRequest = FakeRequest()

      "increment success counter for an informational Result" in withTestActionMetrics { metrics =>
        metrics
          .withMetricsTimerAction(TestMetric) {
            metrics.Action(Results.SwitchingProtocols)
          }
          .apply(fakeRequest)
          .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "increment success counter for a successful Result" in withTestActionMetrics { metrics =>
        metrics
          .withMetricsTimerAction(TestMetric) {
            metrics.Action(Results.Ok)
          }
          .apply(fakeRequest)
          .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "increment success counter for a redirect Result" in withTestActionMetrics { metrics =>
        metrics
          .withMetricsTimerAction(TestMetric) {
            metrics.Action(Results.Found("https://wikipedia.org"))
          }
          .apply(fakeRequest)
          .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "increment failure counter for a client error Result" in withTestActionMetrics { metrics =>
        metrics
          .withMetricsTimerAction(TestMetric) {
            metrics.Action(Results.Conflict)
          }
          .apply(fakeRequest)
          .map(_ => verifyCompletedWithFailure(TestMetric, metrics))
      }

      "increment failure counter for a server error Result" in withTestActionMetrics { metrics =>
        metrics
          .withMetricsTimerAction(TestMetric) {
            metrics.Action(Results.ServiceUnavailable)
          }
          .apply(fakeRequest)
          .map(_ => verifyCompletedWithFailure(TestMetric, metrics))
      }
    }
  }

}
